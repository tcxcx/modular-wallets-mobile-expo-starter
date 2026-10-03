from pathlib import Path
import json
root = Path(__file__).parent
for file in root.rglob('*'):
    if not file.is_file() or '.git' in file.parts: continue
    assert file.name != '.env', file
    text = file.read_text()
    for marker in ['@'+'bu/', 'plan'+'Ceremony', 'execute'+'CeremonyPlan', 'alch'+'__']:
        assert marker not in text, file
config = json.loads((root/'modules/circle-modular-wallets/expo-module.config.json').read_text())
assert config['platforms'] == ['apple', 'android']
assert config['android']['modules'][0].startswith('com.example.')
print('Source boundary and module configuration checks passed')
