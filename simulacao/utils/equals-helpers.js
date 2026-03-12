

export const pessoaEquals = (pessoa1, pessoa2) => {
  console.log(`Comparando pessoas: ${pessoa1} e ${pessoa2}`);
  return (
    pessoa1.nome === pessoa2.nome &&
    pessoa1.email === pessoa2.email &&
    pessoa1.idade === pessoa2.idade
  );
}