export function normalizeProfile(value) {
  if (!value) {
    return '';
  }
  return value
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
}

export function normalizeExecutor(value) {
  if (!value) {
    return '';
  }
  return value
    .toLowerCase()
    .replace(/_/g, '-')
    .trim();
}

export function alphaCode(seed, length) {
  const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  let out = '';
  let value = seed;
  for (let i = 0; i < length; i += 1) {
    out = letters[value % 26] + out;
    value = Math.floor(value / 26);
  }
  // Garante que o código sempre tenha o tamanho requisitado usando left-padding com 'A'
  return out.padStart(length, 'A');
}

export function uniqueSeed(vu, iter) {
  return (vu * 1000000) + iter;
}

export function hashString(value) {
  let hash = 0;
  for (let i = 0; i < value.length; i += 1) {
    hash = ((hash << 5) - hash) + value.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

export function isoDate(date) {
  return date.toISOString().slice(0, 10);
}

export function expect2xx(res, label, failFn, checkFn) {
  const ok = checkFn(res, {
    [`${label} status 2xx`]: (r) => r.status >= 200 && r.status < 300,
  });

  if (!ok) {
    failFn(`${label} falhou status=${res.status} body=${res.body}`);
  }
}
