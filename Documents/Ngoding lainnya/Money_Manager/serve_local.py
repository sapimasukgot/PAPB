import os
import sys
import socket
import json
import shutil
import re
from http.server import SimpleHTTPRequestHandler, HTTPServer

PORT = 8080
BUILD_DIR = "local_release_server"

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(('8.8.8.8', 1))
        ip = s.getsockname()[0]
    except Exception:
        ip = '127.0.0.1'
    finally:
        s.close()
    return ip

def get_version_name():
    version_name = "1.0.3"
    try:
        with open(os.path.join("app", "build.gradle.kts"), "r", encoding="utf-8") as f:
            for line in f:
                if "versionName" in line:
                    match = re.search(r'["\']([^"\']+)["\']', line)
                    if match:
                        return match.group(1)
    except Exception:
        pass
    return version_name

def prepare_server(ip):
    os.makedirs(BUILD_DIR, exist_ok=True)

    src_apk = os.path.join("app", "build", "outputs", "apk", "debug", "app-debug.apk")
    dest_apk = os.path.join(BUILD_DIR, "MoneyManager.apk")
    if os.path.exists(src_apk):
        shutil.copyfile(src_apk, dest_apk)

    file_size = os.path.getsize(dest_apk) if os.path.exists(dest_apk) else 0
    version_name = get_version_name()

    manifest = {
        "tag_name": f"v{version_name}",
        "name": f"Money Manager v{version_name}",
        "body": "Pembaruan filter notifikasi (anti promo Rp 1, penanganan refund, dan anti-duplikat).",
        "assets": [
            {
                "name": "MoneyManager.apk",
                "size": file_size,
                "browser_download_url": f"http://{ip}:{PORT}/MoneyManager.apk"
            }
        ]
    }

    manifest_path = os.path.join(BUILD_DIR, "manifest.json")
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2)

    apphub_src = os.path.join("..", "AppHub", "app", "build", "outputs", "apk", "debug", "app-debug.apk")
    if os.path.exists(apphub_src):
        shutil.copyfile(apphub_src, os.path.join(BUILD_DIR, "AppHub.apk"))

    html_content = f"""<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Money Manager - Unduh Pembaruan</title>
    <style>
        body {{
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #0f172a;
            color: #f8fafc;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
            padding: 20px;
            box-sizing: border-box;
        }}
        .card {{
            background: #1e293b;
            border-radius: 20px;
            padding: 28px;
            max-width: 400px;
            width: 100%;
            box-shadow: 0 10px 25px rgba(0,0,0,0.5);
            text-align: center;
            border: 1px solid #334155;
        }}
        .badge {{
            display: inline-block;
            background: #0ea5e9;
            color: white;
            padding: 4px 12px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
            margin-bottom: 12px;
        }}
        h1 {{
            font-size: 22px;
            margin: 0 0 8px 0;
        }}
        p {{
            color: #94a3b8;
            font-size: 14px;
            margin: 0 0 24px 0;
            line-height: 1.5;
        }}
        .btn {{
            display: block;
            width: 100%;
            background: linear-gradient(135deg, #2563eb, #06b6d4);
            color: white;
            text-decoration: none;
            padding: 14px 0;
            border-radius: 12px;
            font-weight: bold;
            font-size: 15px;
            margin-bottom: 12px;
            box-sizing: border-box;
        }}
        .btn-sub {{
            background: #334155;
            color: #cbd5e1;
        }}
    </style>
</head>
<body>
    <div class="card">
        <span class="badge">Versi v{version_name}</span>
        <h1>Money Manager</h1>
        <p>Pembaruan filter scraping notifikasi (anti promo Rp 1, perbaikan refund, dan anti-duplikat).</p>
        <a href="/MoneyManager.apk" class="btn">⬇️ Unduh Money Manager v{version_name}</a>
        <a href="/manifest.json" class="btn btn-sub" style="font-size: 12px; padding: 10px 0;">Lihat Manifest JSON</a>
    </div>
</body>
</html>
"""
    with open(os.path.join(BUILD_DIR, "index.html"), "w", encoding="utf-8") as f:
        f.write(html_content)

class CustomHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, OPTIONS')
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        super().end_headers()

def run_server(ip):
    prepare_server(ip)
    os.chdir(BUILD_DIR)
    httpd = HTTPServer(('0.0.0.0', PORT), CustomHandler)
    print("===================================================")
    print(" [OK] SERVER PEMBARUAN WI-FI LOKAL AKTIF!")
    print("===================================================")
    print(f" URL Manifest Update: http://{ip}:{PORT}/manifest.json")
    print(f" URL File APK       : http://{ip}:{PORT}/MoneyManager.apk")
    print(f" Web Download Page  : http://{ip}:{PORT}/")
    print("===================================================\n")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServer dimatikan.")

if __name__ == "__main__":
    ip = get_local_ip()
    run_server(ip)
