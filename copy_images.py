import os
import shutil
import re

ios_assets = "/Users/dante/Documents/dante/Documents/eater/eater/Assets.xcassets"
android_drawable = "/Users/dante/Documents/dante/AndroidStudioProjects/Eateria/app/src/main/res/drawable"

os.makedirs(android_drawable, exist_ok=True)

for root, dirs, files in os.walk(ios_assets):
    if root.endswith(".imageset"):
        imageset_name = os.path.basename(root).replace(".imageset", "")
        # Valid drawable name: lowercase, a-z0-9_
        valid_name = re.sub(r'[^a-z0-9_]', '_', imageset_name.lower())
        
        # Find the image file (skip .json and .DS_Store)
        img_file = None
        for f in files:
            if not f.endswith(".json") and not f == ".DS_Store":
                img_file = f
                break
                
        if img_file:
            src = os.path.join(root, img_file)
            ext = os.path.splitext(img_file)[1].lower()
            dst = os.path.join(android_drawable, valid_name + ext)
            shutil.copy2(src, dst)
            print(f"Copied {src} to {dst}")
