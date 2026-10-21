using System;

// Classe Admin herdando da classe Pessoa (Herança)
public class Admin : Pessoa
{
    // Identificador único do administrador
    public int IdAdmin { get; set; }

    // ID da ONG vinculada ao administrador
    public int IdOng { get; set; }

    // Senha do administrador
    public string Senha { get; set; }

    // Sobrescrita do método ExibirDados (Polimorfismo)
    public override void ExibirDados()
    {
        Console.WriteLine($"Administrador: {Nome}");

        Console.WriteLine($"Email: {Email}");
    }
}