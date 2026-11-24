import { createHash } from 'node:crypto';

/**
 * Retrouve l'identifiant du compte n°N créé par scripts/generate-data.sql.
 * Le script SQL fait : md5('a' || N)::uuid  → on refait exactement le même calcul ici.
 */
export function compteDeDemo(numero: number): string {
  const hex = createHash('md5').update(`a${numero}`).digest('hex');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}
