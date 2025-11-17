import yaml
import os

CONFIG_FILE_PATH = os.path.join(os.path.dirname(__file__), 'config.yaml')
CONFIG = None

def load_config():
    global CONFIG
    if CONFIG is None:
        try:
            with open(CONFIG_FILE_PATH, 'r') as file:
                CONFIG = yaml.safe_load(file)
        except FileNotFoundError:
            print(f"ERRORE: File di configurazione '{CONFIG_FILE_PATH}' non trovato.")
            CONFIG = {}
        except yaml.YAMLError as exc:
            print(f"ERRORE YAML: {exc}")
            CONFIG = {}
    return CONFIG


load_config()