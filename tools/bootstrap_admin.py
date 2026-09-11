"""Initialize an empty installation with a user-supplied admin password; no default credentials."""
import os
import secrets
import bcrypt
import migrate


def main():
    password=os.environ['BIZ_BOOTSTRAP_ADMIN_PASSWORD']
    if len(password)<12:raise SystemExit('Admin password must contain at least 12 characters')
    with migrate.connect() as db:
        if migrate.query(db,'SELECT id FROM system_users LIMIT 1'):raise SystemExit('Users already exist; bootstrap refused')
        db.begin()
        try:
            encoded=bcrypt.hashpw(password.encode(),bcrypt.gensalt()).decode()
            migrate.execute(db,"INSERT INTO system_users(id,tenant_id,username,password,nickname,status) VALUES(1,1,'admin',%s,'管理员',0)",(encoded,))
            migrate.execute(db,"INSERT INTO system_user_role(user_id,role_id,tenant_id) VALUES(1,1,1)")
            migrate.execute(db,"INSERT INTO biz_employee(tenant_id,user_id,emp_no,emp_name,status) VALUES(1,1,'ADMIN','管理员','0')")
            migrate.execute(db,"""INSERT INTO system_oauth2_client(client_id,secret,name,logo,status,access_token_validity_seconds,
                refresh_token_validity_seconds,redirect_uris,authorized_grant_types) VALUES('default',%s,'管理后台','',0,1800,86400,'[]','["password","refresh_token"]')""",(secrets.token_urlsafe(32),))
            db.commit()
        except Exception:
            db.rollback();raise
    print('Admin initialized. Credentials were not printed.')


if __name__=='__main__':main()
