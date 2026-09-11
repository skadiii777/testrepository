"""Run InnoDB concurrency tests in a fresh, isolated database; never use the application database.

Set BIZ_DB_HOST/PORT/USER/PASSWORD. --local-config explicitly reads this checkout's local settings.
The generated test database is retained for inspection. This command does not touch business data.
"""
import argparse
import os
import re
import shutil
import subprocess
import time
from pathlib import Path

import pymysql
import migrate


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--local-config',action='store_true')
    parser.add_argument('--offline',action='store_true')
    parser.add_argument('--settings',type=Path)
    args=parser.parse_args()
    env=os.environ.copy()
    if args.local_config:
        config=(migrate.ROOT/'enterprise-server/src/main/resources/application-local.yaml').read_text(encoding='utf-8')
        url=re.search(r'jdbc:mysql://([^:/]+):(\d+)/([^?\s]+)',config)
        auth=re.search(r'username:\s*([^\s#]+)\s*\n\s*password:\s*([^\s#]+)',config)
        password=auth[2].strip('"\'')
        placeholder=re.fullmatch(r'\$\{([^:}]+):?[^}]*}',password)
        if placeholder:
            private=migrate.ROOT/'application-private.properties'
            values=dict(line.split('=',1) for line in private.read_text(encoding='utf-8').splitlines() if '=' in line and not line.startswith('#')) if private.exists() else {}
            password=env.get(placeholder[1],values.get(placeholder[1],''))
        env.update(BIZ_DB_HOST=url[1],BIZ_DB_PORT=url[2],BIZ_DB_USER=auth[1],BIZ_DB_PASSWORD=password)
    database='enterprise_pro_qa_'+time.strftime('%Y%m%d_%H%M%S')
    host=env.get('BIZ_DB_HOST','127.0.0.1');port=int(env.get('BIZ_DB_PORT','3306'))
    with pymysql.connect(host=host,port=port,user=env['BIZ_DB_USER'],password=env['BIZ_DB_PASSWORD'],
                         autocommit=True,charset='utf8mb4',cursorclass=pymysql.cursors.DictCursor) as db:
        migrate.execute(db,'CREATE DATABASE `'+database+'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
        db.select_db(database)
        migrate.run(db,True)
        migrate.run(db,True)
    env.update(BIZ_TEST_JDBC_URL=f'jdbc:mysql://{host}:{port}/{database}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai',
               BIZ_TEST_DB_USER=env['BIZ_DB_USER'],BIZ_TEST_DB_PASSWORD=env['BIZ_DB_PASSWORD'])
    java=shutil.which('java')
    if java:env['JAVA_HOME']=str(Path(java).resolve().parent.parent)
    command=[shutil.which('mvn') or 'mvn','-pl','enterprise-module-biz','-am','test',
             '-Dtest=BizConcurrencyTest','-Dsurefire.failIfNoSpecifiedTests=false']
    if args.offline:command.append('-o')
    if args.settings:command.extend(['-s',str(args.settings.resolve())])
    print('Isolated test database:',database,flush=True)
    result=subprocess.run(command,cwd=migrate.ROOT,env=env)
    raise SystemExit(result.returncode)


if __name__=='__main__':main()
