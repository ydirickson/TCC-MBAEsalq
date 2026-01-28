export function parseEnvFile(source) {
  const env = {};
  const lines = source.split(/\r?\n/);

  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) {
      continue;
    }

    const idx = trimmed.indexOf('=');
    if (idx === -1) {
      continue;
    }

    const key = trimmed.slice(0, idx).trim();
    let value = trimmed.slice(idx + 1).trim();

    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }

    env[key] = value;
  }

  return env;
}

function resolvePath(candidate) {
  if (!candidate) {
    return candidate;
  }
  if (candidate.startsWith('file://') || candidate.startsWith('/')) {
    return candidate;
  }
  const normalized = candidate.startsWith('./') ? candidate.slice(2) : candidate;
  return import.meta.resolve(`../../../${normalized}`);
}

export function loadEnvFile(candidates) {
  for (const path of candidates) {
    if (!path) {
      continue;
    }
    try {
      return parseEnvFile(open(resolvePath(path)));
    } catch (err) {
      // tenta o proximo caminho
    }
  }

  return {};
}

export function makeEnvReaders({ envVars, envFile }) {
  const envValue = (name, fallback) => {
    if (envVars[name] !== undefined && envVars[name] !== '') {
      return envVars[name];
    }
    if (envFile[name] !== undefined && envFile[name] !== '') {
      return envFile[name];
    }
    return fallback;
  };

  const envNumber = (name, fallback) => {
    const raw = envValue(name, undefined);
    if (raw === undefined) {
      return fallback;
    }
    const parsed = Number(raw);
    if (Number.isNaN(parsed)) {
      throw new Error(`Valor invalido para ${name}: ${raw}`);
    }
    return parsed;
  };

  const envJson = (name, fallback) => {
    const raw = envValue(name, undefined);
    if (raw === undefined) {
      return fallback;
    }
    try {
      return JSON.parse(raw);
    } catch (err) {
      throw new Error(`JSON invalido em ${name}: ${raw}`);
    }
  };

  return { envValue, envNumber, envJson };
}
