using System;

// Classe responsável pelos interesses cadastrados no sistema
public class Interesse
{
    // Identificador único do interesse
    public int IdInteresse { get; set; }

    // ID do usuário relacionado ao interesse
    public int IdUsuario { get; set; }

    // Tipo do interesse
    // Exemplo: Doador, Receptor ou Ambos
    public string Tipo { get; set; }

    // Descrição da necessidade ou doação
    public string Descricao { get; set; }

    // ID do status do interesse
    public int IdStatus { get; set; }

    // Data de registro do interesse
    public DateTime DataRegistro { get; set; }
}