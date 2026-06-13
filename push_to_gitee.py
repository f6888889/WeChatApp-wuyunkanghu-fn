import urllib.request
import urllib.parse
import json
import ssl
import base64
import os
import time

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

token = 'b25cbd7203cba3ad4a11c54f7ba868e6'
owner = 'liang-fengg'
repo = 'wxyunwu'
base_url = f'https://gitee.com/api/v5/repos/{owner}/{repo}/contents'

project_root = r'c:\Users\21166\Desktop\dachuang trea'

exclude_patterns = [
    '.git', '.mvn', 'target', 'node_modules', '.vscode',
    '__pycache__', '.idea', 'temp_ports.txt', 'git.zip',
    'Dockerfile', '.dockerignore', 'DEPLOY.md', 'DESIGN_GUIDE.md',
    'generate_icons.py', '.cloudbase'
]

def should_exclude(path):
    path_str = str(path)
    for pattern in exclude_patterns:
        if pattern in path_str:
            return True
    return False

def push_file(file_path, content):
    relative_path = os.path.relpath(file_path, project_root).replace('\\', '/')
    url = f'{base_url}/{urllib.parse.quote(relative_path)}'
    encoded_content = base64.b64encode(content.encode('utf-8')).decode('utf-8')

    data = json.dumps({
        'access_token': token,
        'content': encoded_content,
        'message': f'Add {relative_path}'
    }).encode('utf-8')

    req = urllib.request.Request(url, data=data, method='POST')
    req.add_header('Content-Type', 'application/json')

    try:
        resp = urllib.request.urlopen(req, timeout=30, context=ctx)
        result = json.loads(resp.read())
        return True, relative_path
    except urllib.error.HTTPError as e:
        error_body = e.read().decode('utf-8')
        if e.code == 422:
            put_data = json.dumps({
                'access_token': token,
                'content': encoded_content,
                'message': f'Update {relative_path}'
            }).encode('utf-8')
            put_req = urllib.request.Request(url, data=put_data, method='PUT')
            put_req.add_header('Content-Type', 'application/json')
            try:
                put_resp = urllib.request.urlopen(put_req, timeout=30, context=ctx)
                return True, relative_path
            except Exception as ex:
                return False, f'PUT Error: {ex}'
        return False, f'HTTP {e.code}: {error_body[:200]}'
    except Exception as e:
        return False, str(e)

def get_all_files():
    files = []
    for root, dirs, filenames in os.walk(project_root):
        dirs[:] = [d for d in dirs if not should_exclude(d)]
        for filename in filenames:
            file_path = os.path.join(root, filename)
            if not should_exclude(file_path):
                files.append(file_path)
    return files

print('Scanning project files...')
all_files = get_all_files()
print(f'Found {len(all_files)} files')

success_count = 0
fail_count = 0

for i, file_path in enumerate(all_files):
    relative_path = os.path.relpath(file_path, project_root).replace('\\', '/')
    print(f'[{i+1}/{len(all_files)}] Pushing {relative_path}...', end=' ')

    try:
        with open(file_path, 'rb') as f:
            content = f.read()

        if len(content) > 50 * 1024 * 1024:
            print(f'SKIP (file too large: {len(content)} bytes)')
            fail_count += 1
            continue

        success, msg = push_file(file_path, content.decode('utf-8', errors='replace'))
        if success:
            print('OK')
            success_count += 1
        else:
            print(f'FAIL: {msg}')
            fail_count += 1

        time.sleep(0.3)

    except Exception as e:
        print(f'ERROR: {e}')
        fail_count += 1

print(f'\n=== Done ===')
print(f'Success: {success_count}')
print(f'Failed: {fail_count}')
