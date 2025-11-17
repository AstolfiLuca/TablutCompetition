import logging
import sys
from .config_reader import CONFIG

def setup_logger(name):

    logger = logging.getLogger(name)
    if logger.handlers:
        return logger

    level = logging.getLevelName(CONFIG["logger_level"])
    logger.setLevel(level)

    formatter = logging.Formatter('%(asctime)s - %(levelname)s -  %(name)s - %(message)s')

    # Handler (Semplice: scriviamo solo su console per compattezza)
    # Se vuoi anche il file, aggiungeresti un FileHandler qui.
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(formatter)


    logger.addHandler(console_handler)


    return logger
