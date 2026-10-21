using System;

// Classe Usuario herdando da classe Pessoa (Herança)
public class Usuario : Pessoa
{
    // Identificador único do usuário
    public int IdUsuario { get; set; }

    // CPF do usuário
    public string Cpf { get; set; }

    // Endereço do usuário
    public string Endereco { get; set; }

    // Cidade do usuário
    public string Cidade { get; set; }

    // Data de nascimento do usuário
    public DateTime DataNascimento { get; set; }

    // Data de cadastro no sistema
    public DateTime DataCadastro { get; set; }

    // Tipo de participação do usuário
    // Exemplo: Doador, Receptor ou Ambos
    public string TipoParticipacao { get; set; }

    // ID do estado (UF)
    public int IdUf { get; set; }
}