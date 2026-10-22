using System;

// Classe Atendente herdando da classe Pessoa (Herança)
public class Atendente : Pessoa
{
    // Identificador único do atendente
    public int IdAtendente { get; set; }

    // ID da ONG vinculada ao atendente
    public int IdOng { get; set; }

    // Senha do atendente
    public string Senha { get; set; }

    // Sobrescrita do método ExibirDados (Polimorfismo)
    public override void ExibirDados()
    {
        Console.WriteLine($"Atendente: {Nome}");

        Console.WriteLine($"Email: {Email}");
    }
}