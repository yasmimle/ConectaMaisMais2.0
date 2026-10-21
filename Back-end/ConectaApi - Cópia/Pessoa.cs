// Classe base Pessoa (Herança)
public class Pessoa
{
    // Nome da pessoa
    public string Nome { get; set; }

    // Email da pessoa
    public string Email { get; set; }

    // Telefone da pessoa
    public string Telefone { get; set; }

    // Método virtual para permitir Polimorfismo
    public virtual void ExibirDados()
    {
        Console.WriteLine($"Nome: {Nome}");
        Console.WriteLine($"Email: {Email}");
        Console.WriteLine($"Telefone:{Telefone}");
    }
}