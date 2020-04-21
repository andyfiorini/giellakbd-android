# Unicode Emoji to XML converter

This script aims to convert the emoji-test.txt to a Android XML resource file inorder to be used by the Giella-IME to show the latest emojis.

## Usage

Grab the latest emoji-test.txt file from https://unicode.org/Public/emoji/13.0/

`usage: emoji_converter.py [-h] -i INPUT_FILE -o OUTPUT_FILE`

E.g

`$ ./emoji_converter.py -i emoji-test.txt -o path/to/giella-ime/app/src/main/res/values/emoji-categories-generated.xml`

## Support

Currently this script is only verified to work with Unicode 13.0. The version is subject to change as new releases are made.

