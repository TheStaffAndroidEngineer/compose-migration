#!/usr/bin/env python3
"""Host-only screenshot driver. Uses observed UI bounds; does not alter app state directly."""
import json, re, subprocess, time, xml.etree.ElementTree as ET
from pathlib import Path
ADB = str(Path.home() / "Library/Android/sdk/platform-tools/adb")
SERIAL = "emulator-5554"
PACKAGE = "staff.android.migration"
OUT = Path(__file__).resolve().parents[1] / "evidence/2026-10-07-common-goods/ui"
OUT.mkdir(parents=True, exist_ok=True)
def adb(*args): return subprocess.check_output([ADB, "-s", SERIAL, *args])
def shell(*args): return adb("shell", *args).decode().strip()
def tree():
    for _ in range(4):
        shell("rm", "-f", "/sdcard/migration-review.xml")
        result = shell("uiautomator", "dump", "/sdcard/migration-review.xml")
        if "UI hier" in result:
            raw = adb("exec-out", "cat", "/sdcard/migration-review.xml")
            return raw, ET.fromstring(raw)
        time.sleep(.6)
    raise RuntimeError("Could not capture fresh hierarchy")
def tap(value, attr="text"):
    _, root = tree()
    hits = [n for n in root.iter("node") if n.get(attr) == value]
    if len(hits) != 1: raise RuntimeError(f"Expected one {value}; found {len(hits)}")
    n = hits[0]
    if n.get("enabled") != "true": raise RuntimeError("Disabled: " + value)
    x1,y1,x2,y2 = map(int,re.findall(r"\d+", n.get("bounds")))
    shell("input", "tap", str((x1+x2)//2), str((y1+y2)//2)); time.sleep(.45)
def tapid(value): tap(PACKAGE + ":id/" + value, "resource-id")
def capture(name):
    time.sleep(.6); raw,_ = tree()
    (OUT / (name + ".xml")).write_bytes(raw)
    (OUT / (name + ".png")).write_bytes(adb("exec-out", "screencap", "-p"))
    print(name, flush=True)
def start():
    shell("am", "force-stop", PACKAGE)
    shell("am", "start", "-W", "-n", PACKAGE + "/.MainActivity"); time.sleep(1)
def scroll(container, reverse=False):
    _, root = tree()
    hits = [n for n in root.iter("node") if n.get("resource-id") == PACKAGE + ":id/" + container]
    if not hits: raise RuntimeError("Missing scroll container: " + container)
    x1,y1,x2,y2=map(int,re.findall(r"\d+",hits[0].get("bounds")))
    ya,yb = y1+(y2-y1)*8//10,y1+(y2-y1)*2//10
    if reverse: ya,yb=yb,ya
    shell("input", "swipe", str((x1+x2)//2), str(ya), str((x1+x2)//2), str(yb), "450"); time.sleep(.4)
if __name__ == "__main__":
    import sys
    if sys.argv[1] == "start": start()
    elif sys.argv[1] == "capture": capture(sys.argv[2])
    elif sys.argv[1] == "tapid": tapid(sys.argv[2])
    elif sys.argv[1] == "tap": tap(sys.argv[2])
    elif sys.argv[1] == "scroll": scroll(sys.argv[2])
    elif sys.argv[1] == "nodes":
        _, root = tree()
        print(json.dumps([{k:n.get(k) for k in ("text", "content-desc", "resource-id", "bounds")} for n in root.iter("node") if n.get("text") or n.get("content-desc")], indent=2))
