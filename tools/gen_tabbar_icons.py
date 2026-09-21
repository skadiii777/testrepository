#!/usr/bin/env python3
"""生成移动端 tabBar 图标（8 个 PNG）。

uni-app 的 tabBar iconPath 只支持位图，因此用 PIL 程序化生成，
避免手工导出、也便于改主色后一键重跑：

    python tools/gen_tabbar_icons.py

输出：src/static/tabbar/{msg,work,approve,mine}[-on].png
色值：未选中 #9CA3AF / 选中 #0E7A63（与 src/styles/tokens.scss 保持一致）
"""
import os

from PIL import Image, ImageDraw

SIZE = 81
STROKE = 7
OFF = (156, 163, 175, 255)   # #9CA3AF
ON = (14, 122, 99, 255)      # #0E7A63

OUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "static", "tabbar")


def canvas():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)


def icon_msg(color):
    """消息：圆角气泡 + 左下尾巴"""
    img, d = canvas()
    d.rounded_rectangle((11, 13, 70, 57), radius=13, outline=color, width=STROKE)
    d.polygon([(23, 53), (23, 72), (40, 55)], fill=color)
    return img


def icon_work(color):
    """工作台：2x2 圆角方块"""
    img, d = canvas()
    for box in ((11, 11, 37, 37), (44, 11, 70, 37), (11, 44, 37, 70), (44, 44, 70, 70)):
        d.rounded_rectangle(box, radius=8, outline=color, width=STROKE)
    return img


def icon_approve(color):
    """审批：对勾"""
    img, d = canvas()
    d.line([(17, 41), (33, 57), (64, 23)], fill=color, width=STROKE + 1, joint="curve")
    return img


def icon_mine(color):
    """我的：头部圆 + 肩部弧"""
    img, d = canvas()
    d.ellipse((28, 14, 53, 39), outline=color, width=STROKE)
    d.arc((15, 42, 66, 78), start=180, end=360, fill=color, width=STROKE)
    return img


ICONS = {
    "msg": icon_msg,
    "work": icon_work,
    "approve": icon_approve,
    "mine": icon_mine,
}


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name, fn in ICONS.items():
        fn(OFF).save(os.path.join(OUT_DIR, f"{name}.png"))
        fn(ON).save(os.path.join(OUT_DIR, f"{name}-on.png"))
        print(f"generated {name}.png / {name}-on.png")
    print(f"done -> {os.path.normpath(OUT_DIR)}")


if __name__ == "__main__":
    main()
