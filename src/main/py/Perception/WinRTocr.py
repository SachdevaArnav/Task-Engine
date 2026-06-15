import mss, ctypes, asyncio

from winrt.windows.graphics.imaging import (
    SoftwareBitmap,
    BitmapPixelFormat,
    BitmapBufferAccessMode
)

from winrt.windows.media.ocr import OcrEngine
from winrt.windows.globalization import Language


# ---------------------------
# 1. SCREENSHOT (BGRA RAW)
# ---------------------------
def capture_screen():
    with mss.mss() as sct:
        monitor = sct.monitors[0] #combined area of all connected monitors
        shot = sct.grab(monitor)

        width = shot.width
        height = shot.height
        bgra = shot.bgra  # raw bytes-like object

        return bgra, width, height, monitor


# ---------------------------
# 2. BGRA → SoftwareBitmap
# ---------------------------
def to_software_bitmap(bgra, width, height):
    bitmap = SoftwareBitmap(BitmapPixelFormat.BGRA8, width, height)

    buffer = bitmap.lock_buffer(BitmapBufferAccessMode.WRITE)
    reference = buffer.create_reference()

    ctypes.memmove(
        ctypes.addressof(ctypes.c_char.from_buffer(reference)),
        bgra,
        len(bgra)
    )

    return bitmap


# ---------------------------
# 3. OCR ENGINE INIT
# ---------------------------
engine = OcrEngine.try_create_from_language(Language("en"))


# ---------------------------
# 4. OCR EXECUTION
# ---------------------------
async def run_ocr(bitmap):
    result = await engine.recognize_async(bitmap)
    return result


# ---------------------------
# 5. PARSE OUTPUT
# ---------------------------
def parse_result(result, offset_x=0, offset_y=0):
    output = []

    for line in result.lines:
        for word in line.words:
            rect = word.bounding_rect

            output.append({
                "text": word.text,
                "x": rect.x + offset_x,
                "y": rect.y + offset_y,
                "w": rect.width,
                "h": rect.height
            })

    return output


# ---------------------------
# 6. PIPELINE WRAPPER
# ---------------------------
async def ocr_pipeline():
    bgra, width, height, monitor = capture_screen()

    bitmap = to_software_bitmap(bgra, width, height)

    result = await run_ocr(bitmap)

    return{"status":"ok"
        ,"matches": parse_result(
        result,
        offset_x=monitor["left"],
        offset_y=monitor["top"]
    )}

def run():
    return asyncio.run(ocr_pipeline())
