#!/usr/bin/env python3
"""Reproduce the checked Chapter 6 screenshots, then restore device settings."""
import importlib.util, json, time, sys
from pathlib import Path
spec=importlib.util.spec_from_file_location('capture',Path(__file__).with_name('capture-ui.py'))
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
out=m.OUT.parent
original={'font_scale':m.shell('settings','get','system','font_scale'),'night':m.shell('cmd','uimode','night'),'size':m.shell('wm','size'),'density':m.shell('wm','density')}
(out/'device-before-review.json').write_text(json.dumps(original,indent=2)+'\n')
try:
    if '--config-only' not in sys.argv:
        m.tapid('lab_controls_toggle');m.capture('02-views-controls')
        m.tapid('mode_compose');m.capture('03-compose-controls')
        m.tapid('lab_controls_toggle');m.capture('04-compose')
        m.tapid('lab_controls_toggle');m.tapid('mode_bug');m.tapid('rename_camera');m.capture('05-stale')
        m.tapid('mode_views');m.tapid('mode_bug');m.capture('06-remounted')
        m.tapid('mode_compose');m.tapid('reset');m.tapid('rename_camera');m.capture('07-compose-observes')
        m.tapid('lab_controls_toggle')
        for title in ['Camera Pro','Everyday bag','Headphones']:m.tap('Remove '+title,'content-desc')
        m.capture('08-compose-empty')
        m.tapid('lab_controls_toggle');m.tapid('mode_views');m.tapid('lab_controls_toggle');m.capture('09-views-empty')
    m.shell('cmd','uimode','night','yes');time.sleep(2);m.start();m.capture('10-views-dark')
    m.tapid('lab_controls_toggle');m.tapid('mode_compose');m.tapid('lab_controls_toggle');m.capture('11-compose-dark')
    m.shell('cmd','uimode','night','no');m.shell('settings','put','system','font_scale','2.0');time.sleep(2);m.start();m.capture('12-views-large-text')
    m.tapid('lab_controls_toggle');m.capture('13-views-large-text-controls')
    m.tapid('mode_compose');m.capture('14-compose-large-text-controls')
    m.tapid('lab_controls_toggle');m.capture('15-compose-large-text')
    m.shell('settings','put','system','font_scale','1.0');m.shell('wm','size','945x2048');time.sleep(2);m.start();m.capture('16-views-narrow')
    m.tapid('lab_controls_toggle');m.tapid('mode_compose');m.tapid('lab_controls_toggle');m.capture('17-compose-narrow')
finally:
    m.shell('settings','put','system','font_scale',original['font_scale'])
    m.shell('cmd','uimode','night',original['night'].split(':')[-1].strip())
    if 'Override size:' in original['size']:m.shell('wm','size',original['size'].split('Override size:')[-1].strip())
    else:m.shell('wm','size','reset')
    time.sleep(2)
    m.start()
    restored={'font_scale':m.shell('settings','get','system','font_scale'),'night':m.shell('cmd','uimode','night'),'size':m.shell('wm','size'),'density':m.shell('wm','density')}
    (out/'device-after-review.json').write_text(json.dumps(restored,indent=2)+'\n')
    assert original == restored,(original,restored)
m.capture('18-delivery-views')
