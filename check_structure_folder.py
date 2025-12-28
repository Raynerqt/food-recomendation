import os

def scan_tree(path, ignore_list=None, indent=""):
    if ignore_list is None:
        ignore_list = []

    # Ambil semua item di folder
    items = sorted(os.listdir(path))

    for item in items:
        full_path = os.path.join(path, item)

        # Cek apakah folder/item ada di ignore list
        if item in ignore_list:
            continue

        # Kalau folder
        if os.path.isdir(full_path):
            print(indent + f"📁 {item}")
            scan_tree(full_path, ignore_list, indent + "   ")
        else:
            print(indent + f"📄 {item}")

# ----------------
# Konfigurasi
# ----------------
target = r"food-recomendation"
ignore_folders = ["venv", "__pycache__", "node_modules", ".git", "tests"]

scan_tree(target, ignore_folders)
