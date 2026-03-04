

export const createPessoaPayload = () => {

    return {
      nome: `Test User ${Math.floor(Math.random() * 1000)}`,
      dataNascimento: '1995-05-15',
      nomeSocial: null,
      documentoIdentificacao: {
        tipo: 'CPF',
        numero: `${Math.floor(Math.random() * 90000000000) + 10000000000}`,
      },
      contato: {
        email: `test${Math.floor(Math.random() * 1000)}@example.com`,
        telefone: '11987654321',
      },
      endereco: {
        logradouro: 'Rua Teste, 123',
        cidade: 'São Paulo',
        uf: 'SP',
        cep: '01234-567',
      },
    }

};