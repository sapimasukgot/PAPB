import os
import sys
import json
import re
import subprocess
import urllib.request
import urllib.error

REPO = "sapimasukgot/PAPB"
APK_PATH = os.path.join("app", "build", "outputs", "apk", "debug", "app-debug.apk")

def get_git_token():
    try:
        p = subprocess.Popen(['git', 'credential', 'fill'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
        out, _ = p.communicate('protocol=https\nhost=github.com\n\n')
        creds = dict(line.split('=', 1) for line in out.strip().split('\n') if '=' in line)
        token = creds.get('password')
        if token:
            return token
    except Exception as e:
        print(f"[!] Warning: Gagal membaca token dari git credential: {e}")
    return None

def get_version_from_gradle():
    gradle_path = os.path.join("app", "build.gradle.kts")
    if os.path.exists(gradle_path):
        with open(gradle_path, "r", encoding="utf-8") as f:
            for line in f:
                if "versionName" in line:
                    match = re.search(r'["\']([^"\']+)["\']', line)
                    if match:
                        return match.group(1)
    return "1.0.2"

def ensure_apk_built():
    if not os.path.exists(APK_PATH):
        print("[*] Meng-compile APK Money Manager terlebih dahulu...")
        gradle_cmd = "gradlew.bat" if os.name == 'nt' else "./gradlew"
        res = subprocess.run([gradle_cmd, "assembleDebug"])
        if res.returncode != 0:
            print("[ERROR] Build APK gagal. Periksa error gradle di atas.")
            sys.exit(1)
    size_mb = os.path.getsize(APK_PATH) / (1024 * 1024)
    print(f"[OK] File APK siap: {APK_PATH} ({size_mb:.2f} MB)")

def create_or_replace_release(token, tag, notes):
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "MoneyManager-Publisher"
    }

    check_url = f"https://api.github.com/repos/{REPO}/releases/tags/{tag}"
    existing_release = None
    try:
        req = urllib.request.Request(check_url, headers=headers)
        with urllib.request.urlopen(req) as resp:
            existing_release = json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        if e.code != 404:
            print(f"[!] Warning cek rilis: HTTP {e.code}")

    if existing_release:
        rel_id = existing_release.get("id")
        print(f"[*] Rilis untuk tag {tag} sudah ada (ID: {rel_id}), memperbarui rilis...")

        del_url = f"https://api.github.com/repos/{REPO}/releases/{rel_id}"
        req = urllib.request.Request(del_url, headers=headers, method="DELETE")
        try:
            with urllib.request.urlopen(req) as resp:
                pass
            print(f"[OK] Rilis lama ID {rel_id} dihapus untuk digantikan yang baru.")
        except Exception as e:
            print(f"[!] Gagal menghapus rilis lama: {e}")

    create_url = f"https://api.github.com/repos/{REPO}/releases"
    payload = {
        "tag_name": tag,
        "name": f"Money Manager {tag}",
        "body": notes,
        "draft": False,
        "prerelease": False
    }
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(create_url, data=data, headers={**headers, "Content-Type": "application/json"}, method="POST")
    try:
        with urllib.request.urlopen(req) as resp:
            release_data = json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode('utf-8')
        print(f"[ERROR] Gagal membuat rilis di GitHub: HTTP {e.code} - {err_msg}")
        sys.exit(1)

    print(f"[OK] Rilis GitHub berhasil dibuat! Tag: {tag}")
    return release_data

def upload_apk_asset(token, release_data, tag):
    upload_url_template = release_data.get("upload_url", "")
    base_upload_url = upload_url_template.split("{")[0]
    asset_name = f"MoneyManager-{tag}.apk"
    upload_url = f"{base_upload_url}?name={asset_name}"

    file_size = os.path.getsize(APK_PATH)
    print(f"[*] Mengunggah {asset_name} ({file_size / (1024 * 1024):.2f} MB) ke GitHub Releases...")

    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "Content-Type": "application/vnd.android.package-archive",
        "Content-Length": str(file_size),
        "User-Agent": "MoneyManager-Publisher"
    }

    with open(APK_PATH, "rb") as f:
        file_bytes = f.read()

    req = urllib.request.Request(upload_url, data=file_bytes, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req) as resp:
            asset_data = json.loads(resp.read().decode('utf-8'))
            download_url = asset_data.get("browser_download_url")
            print(f"[OK] Upload sukses! Download URL: {download_url}")
            return download_url
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode('utf-8')
        print(f"[ERROR] Gagal mengunggah file APK ke rilis: HTTP {e.code} - {err_msg}")
        sys.exit(1)

def main():
    print("===================================================")
    print("      Money Manager - Rilis Pembaruan GitHub       ")
    print("===================================================")

    token = get_git_token()
    if not token:
        print("[ERROR] Token GitHub tidak ditemukan dari git credential manager.")
        sys.exit(1)

    default_version = get_version_from_gradle()
    default_tag = f"v{default_version}"

    if len(sys.argv) > 1:
        tag = sys.argv[1]
    else:
        tag_input = input(f"Masukkan Versi Tag [{default_tag}]: ").strip()
        tag = tag_input if tag_input else default_tag

    if len(sys.argv) > 2:
        notes = sys.argv[2]
    else:
        notes_input = input("Masukkan Catatan Rilis (Enter untuk default): ").strip()
        notes = notes_input if notes_input else "Pembaruan filter scraping notifikasi (pencegahan duplikat, promo Rp 1, dan refund)."

    ensure_apk_built()

    print(f"\n[1/2] Menghubungi GitHub API ({REPO})...")
    release_data = create_or_replace_release(token, tag, notes)

    print(f"\n[2/2] Mengunggah file APK...")
    dl_url = upload_apk_asset(token, release_data, tag)

    print("\n===================================================")
    print(" BERHASIL! Rilis dan APK sudah aktif di GitHub.")
    print(f" Tag rilis  : {tag}")
    print(f" URL Unduh  : {dl_url}")
    print("---------------------------------------------------")
    print(" Sekarang buka aplikasi AppHub di HP-mu:")
    print(" 1. Klik ikon Refresh (putar balik).")
    print(" 2. Tombol 'Unduh & Perbarui' akan langsung muncul!")
    print("===================================================")

if __name__ == "__main__":
    main()
