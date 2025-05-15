import jwt
import time
import requests
import os
import sys

APP_ID = os.getenv("GH_APP_ID")
TARGET_ACCOUNT = os.getenv("TARGET_ACCOUNT")
PRIVATE_KEY_STR = os.getenv("GH_APP_PRIVATE_KEY")

if not APP_ID or not TARGET_ACCOUNT or not PRIVATE_KEY_STR:
    print("❌ 請設定 GH_APP_ID、TARGET_ACCOUNT 與 GH_APP_PRIVATE_KEY")
    sys.exit(1)

def generate_jwt(app_id: str, private_key: str):
    now = int(time.time())
    payload = {
        "iat": now - 60,
        "exp": now + (10 * 60),
        "iss": app_id
    }
    return jwt.encode(payload, private_key, algorithm="RS256")

def get_installation_id(jwt_token: str, target_account: str):
    url = "https://api.github.com/app/installations"
    headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Accept": "application/vnd.github+json"
    }
    resp = requests.get(url, headers=headers)
    resp.raise_for_status()
    for inst in resp.json():
        if inst["account"]["login"].lower() == target_account.lower():
            return inst["id"]
    raise Exception(f"❌ 找不到安裝 ID，target_account: {target_account}")

def get_access_token(jwt_token: str, installation_id: int):
    url = f"https://api.github.com/app/installations/{installation_id}/access_tokens"
    headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Accept": "application/vnd.github+json"
    }
    resp = requests.post(url, headers=headers)
    resp.raise_for_status()
    return resp.json()["token"]

def main():
    jwt_token = generate_jwt(APP_ID, PRIVATE_KEY_STR)
    installation_id = get_installation_id(jwt_token, TARGET_ACCOUNT)
    token = get_access_token(jwt_token, installation_id)
    print(token)

if __name__ == "__main__":
    main()
