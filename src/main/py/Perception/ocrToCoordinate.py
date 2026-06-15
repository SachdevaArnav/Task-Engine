import pytesseract
import cv2
import numpy as np
import mss
import sys
#-------------------------------------------------------------------------------------------
# Shifting to WinRT OCR engine (Windows.Media.Ocr) --introduced in Windows 10
# thourghly checked much superior to pytesseract and plug-and-play for almost all tasks
# Already available on all Windows Machines and already using in everyday
# The Only disadvantage is Windows-specific -- but the execution layer already have a lot of such features
# Any extension to other OS and the thus switching is in some sense a future problem due to contraints of
# the problem of desktop task automation and hence a need for the OS specific modules for them.
# Also this adivce -- might be useful for WinRT based OCR:
# You must pass raw memory byte arrays directly from your vision capture system straight into the WinRT memory stream (InMemoryRandomAccessStream) to keep the loop entirely in the RAM cache.
#------------------------------------------------------------------------------------------
# This module is kept for benchmarking purposes with WinRT OCR or other alternatives.
# -----------------------------------------------------------------------------------------
pytesseract.pytesseract.tesseract_cmd = r"C:\\Program Files\\Tesseract-OCR\\tesseract.exe"
def run(target):
    print(f"[DEBUG] TARGET: [{target}]", file=sys.stderr, flush=True)
    with mss.mss() as screenCaptureTool:
        monitor=screenCaptureTool.monitors[0] #combined area of all connected monitors
        print(monitor, file=sys.stderr, flush=True)
        img = np.array  (screenCaptureTool.grab(monitor))[:, :, :3]
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        gray = cv2.resize(gray, None, fx=2, fy=2)
        inverted_gray=cv2.bitwise_not(gray)
        clahe = cv2.createCLAHE(2.0, (8,8))
        clahe1= cv2.createCLAHE(1.5, (8,8))
        gray = clahe.apply(gray)
        inverted_gray=clahe1.apply(inverted_gray)
        gray = cv2.GaussianBlur(gray, (3,3), 0)
        inverted_gray=cv2.GaussianBlur(inverted_gray,(3,3), 0)
        data=pytesseract.image_to_data(gray,config="--psm 11",output_type=pytesseract.Output.DICT)
        data1=pytesseract.image_to_data(inverted_gray,config="--psm 11",output_type=pytesseract.Output.DICT)
        list=[]
        for i in range(len(data["text"])):
            text = data["text"][i].strip()
            if not text:
                continue
            conf=int(data["conf"][i])
            if  conf< 30:
                continue
            list.append(
                {
                "text":text.lower(),
                "x" : data["left"][i]/2,
                "y" : data["top"][i]/2,
                "w" : data["width"][i]/2,
                "h" : data["height"][i]/2,
                "conf": conf
                }
                )
        for i in range(len(data1["text"])):
            text = data1["text"][i].strip()
            if not text:
                continue
            conf=int(data1["conf"][i])
            if  conf< 30:
                continue
            list.append(
                {
                "text":text.lower(),
                "x" : data1["left"][i]/2,
                "y" : data1["top"][i]/2,
                "w" : data1["width"][i]/2,
                "h" : data1["height"][i]/2,
                "conf": conf
                }
                )

        return {
            "status":"ok",
            "matches":list
        }
