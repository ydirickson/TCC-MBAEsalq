import { fail } from 'k6';

export function loadConfig(name = 'endpoints') {
  const fileName = name.endsWith('.json') ? name : `${name}.json`;
  try {
    const path = import.meta.resolve(`../configs/${fileName}`);
    return JSON.parse(open(path));
  } catch (err) {
    fail(`Nao foi possivel localizar ${fileName} em monitoramento/k6/configs.`);
  }
}
