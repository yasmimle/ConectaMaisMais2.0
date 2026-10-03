/* ============================================================
   VALIDAÇÕES DO FRONT-END
   Mesmas regras do back-end (Program.cs). Cada função devolve
   "" quando está tudo certo, ou a mensagem de erro para exibir.
============================================================ */

const Validacoes = {

    /* tira pontos, traços, parênteses e espaços (deixa só os números) */
    somenteDigitos(texto) {
        return (texto || "").replace(/\D/g, "");
    },

    /* ---------- pessoa ---------- */

    // não vazio e só letras/espaços
    nome(texto) {
        const t = texto || "";
        if (!t.trim() || !/^[\p{L}\s]+$/u.test(t)) {
            return "Nome inválido. Use apenas letras.";
        }
        return "";
    },

    // 11 números, dígitos verificadores corretos e não pode ser repetido
    cpf(texto) {
        const c = this.somenteDigitos(texto);
        if (c.length !== 11 || !this._cpfConfere(c)) {
            return "CPF inválido. Digite 11 números.";
        }
        return "";
    },

    _cpfConfere(cpf) {
        if (/^(\d)\1{10}$/.test(cpf)) return false;

        let soma = 0;
        for (let i = 0; i < 9; i++) soma += Number(cpf[i]) * (10 - i);
        let resto = (soma * 10) % 11;
        if (resto === 10) resto = 0;
        if (resto !== Number(cpf[9])) return false;

        soma = 0;
        for (let i = 0; i < 10; i++) soma += Number(cpf[i]) * (11 - i);
        resto = (soma * 10) % 11;
        if (resto === 10) resto = 0;
        return resto === Number(cpf[10]);
    },

    // CPF já cadastrado? (ignorarIndice = posição do usuário que está sendo editado)
    cpfJaCadastrado(texto, ignorarIndice) {
        const c = this.somenteDigitos(texto);
        const usuarios = JSON.parse(localStorage.getItem("usuariosCadastrados") || "[]");
        return usuarios.some((u, i) =>
            i !== ignorarIndice && this.somenteDigitos(u.cpf) === c
        );
    },

    // DDD + número = 11 números
    telefone(texto) {
        if (this.somenteDigitos(texto).length !== 11) {
            return "Telefone inválido. Digite DDD + número com 11 dígitos.";
        }
        return "";
    },

    /* ---------- e-mail ---------- */

    // cadastro de usuário: opcional, mas se vier precisa ter @
    emailOpcional(texto) {
        const t = (texto || "").trim();
        if (t && !t.includes("@")) return "Email inválido.";
        return "";
    },

    // edição / admin / atendente / ONG: precisa ter @ e .
    email(texto) {
        const t = (texto || "").trim();
        if (!t || !t.includes("@") || !t.includes(".")) return "Email inválido.";
        return "";
    },

    // e-mail já existe na lista? (chave = "administradores" ou "atendentes")
    emailJaCadastrado(chave, texto, emailAtual) {
        const novo = (texto || "").trim().toLowerCase();
        const atual = (emailAtual || "").trim().toLowerCase();
        if (novo === atual) return false;

        const lista = JSON.parse(localStorage.getItem(chave) || "[]");
        return lista.some(p => (p.email || "").trim().toLowerCase() === novo);
    },

    /* ---------- endereço ---------- */

    cep(texto) {
        if (this.somenteDigitos(texto).length !== 8) {
            return "CEP inválido. Digite 8 números.";
        }
        return "";
    },

    // só letras e números
    numeroCasa(texto) {
        const t = (texto || "").trim();
        if (!t || !/^[\p{L}\p{N}]+$/u.test(t)) {
            return "Número inválido. Digite apenas números e letras.";
        }
        return "";
    },

    rua(texto) {
        if (!(texto || "").trim()) return "Rua inválida.";
        return "";
    },

    bairro(texto) {
        const t = texto || "";
        if (!t.trim() || !/^[\p{L}\s]+$/u.test(t)) return "Bairro inválido.";
        return "";
    },

    cidade(texto) {
        const t = texto || "";
        if (!t.trim() || !/^[\p{L}\s]+$/u.test(t)) return "Cidade inválida.";
        return "";
    },

    estado(texto) {
        const ufs = ["AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
                     "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO"];
        if (!ufs.includes((texto || "").trim().toUpperCase())) {
            return "Estado inválido.";
        }
        return "";
    },

    // edição do endereço de um usuário: só não pode ficar vazio
    enderecoUsuario(texto) {
        if (!(texto || "").trim()) return "Endereço inválido.";
        return "";
    },

    // endereço da ONG: pelo menos 5 caracteres e alguma letra
    enderecoOng(texto) {
        const t = (texto || "").trim();
        if (t.length < 5 || !/\p{L}/u.test(t)) return "Endereço inválido.";
        return "";
    },

    /* ---------- outros ---------- */

    // data no formato AAAA-MM-DD (campo type="date")
    dataNascimento(texto) {
        const t = texto || "";
        const partes = t.split("-");
        if (partes.length !== 3) return "Data de nascimento inválida.";

        const [a, m, d] = partes.map(Number);
        const data = new Date(a, m - 1, d);

        if (
            isNaN(data.getTime()) ||
            data.getFullYear() !== a ||
            data.getMonth() !== m - 1 ||
            data.getDate() !== d
        ) {
            return "Data de nascimento inválida.";
        }
        return "";
    },

    senha(texto) {
        if (!(texto || "").trim()) return "Senha inválida.";
        return "";
    }
};
