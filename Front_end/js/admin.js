// ===============================
// MENU LATERAL
// ===============================

function toggleMenu(menuId) {

    const menu = document.getElementById(menuId);

    if (!menu) return;

    menu.classList.toggle("show");
}

// ===============================
// TROCAR TELAS
// ===============================

function showSection(sectionId) {

    const sections = document.querySelectorAll(".section");

    sections.forEach(sec => {
        sec.style.display = "none";
    });

    const section = document.getElementById(sectionId);

    if (section) {
        section.style.display = "block";
    }
}

// ===============================
// PERFIL
// ===============================

function toggleProfile() {

    const dropdown = document.getElementById("profileDropdown");

    if (!dropdown) return;

    dropdown.classList.toggle("hidden");
}

// ===============================
// INICIAR SISTEMA
// ===============================

document.addEventListener("DOMContentLoaded", () => {

    showSection("listarUsuarios");

    carregarUsuarios();
    carregarInteresses();
    carregarAdministradores();
    carregarAtendentes();
    configurarPesquisas();
    carregarDadosONG();
    carregarPerfil();
});
document.addEventListener("DOMContentLoaded", function () {
    const email = localStorage.getItem("adminEmail");
    const emailEl = document.getElementById("adminEmailText");

    if (emailEl && email) {
        emailEl.textContent = email;
    }

    const nome = localStorage.getItem("adminNome");
    const nomeEl = document.getElementById("adminNomeText");

    if (nomeEl && nome) {
        nomeEl.textContent = nome;
    }

    carregarDadosONG();
});

// ===============================
// USUÁRIOS
// ===============================

function carregarUsuarios() {

    const lista = document.getElementById("listaUsuarios");

    if (!lista) return;

    lista.innerHTML = "";

    const usuarios =
        JSON.parse(localStorage.getItem("usuariosCadastrados")) || [];

    usuarios.forEach((usuario, index) => {

        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${usuario.nome || ""}</td>
            <td>${usuario.cpf || ""}</td>
            <td>${usuario.email || ""}</td>
            <td>${usuario.telefone || ""}</td>
            <td>${usuario.endereco || ""}</td>

            <td>
                <div class="dropdown-actions">

                    <button onclick="toggleActions(this)">
                        Ações ▼
                    </button>

                    <div class="actions-menu hidden">

                        <a onclick="abrirEditarUsuario('telefone', ${index})">
                            Editar telefone
                        </a>

                        <a onclick="abrirEditarUsuario('email', ${index})">
                            Editar e-mail
                        </a>

                        <a onclick="abrirEditarUsuario('endereco', ${index})">
                            Editar endereço
                        </a>

                        <a onclick="removerUsuario(${index})">
                            Remover
                        </a>

                    </div>
                </div>
            </td>
        `;

        lista.appendChild(tr);
    });
}

function removerUsuario(index) {

    const confirmar = confirm(
        "Tem certeza que deseja remover este usuário?"
    );

    if (!confirmar) return;

    const usuarios =
        JSON.parse(localStorage.getItem("usuariosCadastrados")) || [];

    usuarios.splice(index, 1);

    localStorage.setItem(
        "usuariosCadastrados",
        JSON.stringify(usuarios)
    );

    carregarUsuarios();
}

// ===============================
// INTERESSES
// ===============================

function carregarInteresses() {

    const lista = document.getElementById("listaInteresses");

    if (!lista) return;

    lista.innerHTML = "";

    const interesses =
        JSON.parse(localStorage.getItem("interessesCadastrados")) || [];

    interesses.forEach((item, index) => {

        const statusTexto = item.status || "Pendente";
        const statusLower = statusTexto.toLowerCase();

        const classeStatus =
            statusLower.includes("andamento") ? "andamento" :
                statusLower.includes("conclu") ? "concluido" : "pendente";

        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${item.nome || ""}</td>

            <td>
                <span class="tipo ${item.tipo || ""}">
                    ${item.tipo || ""}
                </span>
            </td>

            <td>${item.necessidade || ""}</td>

            <td>
                <span class="${classeStatus}">
                    ${statusTexto}
                </span>
            </td>

            <td>${item.dataSolicitacao || ""}</td>

            <td>
                <div class="dropdown-actions">

                    <button onclick="toggleActions(this)">
                        Ações ▼
                    </button>

                    <div class="actions-menu hidden">

                        <a onclick="abrirEditarInteresse('status', ${index})">
                            Editar status
                        </a>

                        <a onclick="abrirEditarInteresse('descricao', ${index})">
                            Editar descrição
                        </a>

                        <a onclick="abrirEditarInteresse('tipo', ${index})">
                            Editar tipo
                        </a>

                        <a onclick="removerInteresse(${index})">
                            Remover
                        </a>

                    </div>
                </div>
            </td>
        `;

        lista.appendChild(tr);
    });
}
// ===============================
// ADMINISTRADORES
// ===============================

function carregarAdministradores() {

    const lista = document.getElementById("listaAdministradores");

    if (!lista) return;

    lista.innerHTML = "";

    const administradores =
        JSON.parse(localStorage.getItem("administradores")) || [];

    administradores.forEach((admin, index) => {

        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${admin.nome || ""}</td>

            <td>${admin.email || ""}</td>

            <td>
                <div class="senha-box">

                    <span id="senha-admin-${index}">
                        ••••••
                    </span>

                    <button
                        class="btn-olho"
                        onclick="toggleSenha('senha-admin-${index}', '${admin.senha || ""}')"
                    >
                        <i class="fa-regular fa-eye"></i>
                    </button>

                </div>
            </td>

            <td>
                <div class="dropdown-actions">

                    <button onclick="toggleActions(this)">
                        Ações ▼
                    </button>

                    <div class="actions-menu hidden">

                        <a onclick="abrirEdicaoCadastro('admin', 'email', ${index})">
                            Editar e-mail
                        </a>

                        <a onclick="abrirEdicaoCadastro('admin', 'senha', ${index})">
                            Editar senha
                        </a>

                        <a onclick="removerCadastro('admin', ${index})">
                            Remover
                        </a>

                    </div>
                </div>
            </td>
        `;

        lista.appendChild(tr);
    });
}

// ===============================
// ATENDENTES
// ===============================

function carregarAtendentes() {

    const lista = document.getElementById("listaAtendentes");

    if (!lista) return;

    lista.innerHTML = "";

    const atendentes =
        JSON.parse(localStorage.getItem("atendentes")) || [];

    atendentes.forEach((atendente, index) => {

        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>${atendente.nome || ""}</td>

            <td>${atendente.email || ""}</td>

            <td>
                <div class="senha-box">

                    <span id="senha-atendente-${index}">
                        ••••••
                    </span>

                    <button
                        class="btn-olho"
                        onclick="toggleSenha('senha-atendente-${index}', '${atendente.senha || ""}')"
                    >
                        <i class="fa-regular fa-eye"></i>
                    </button>

                </div>
            </td>

            <td>
                <div class="dropdown-actions">

                    <button onclick="toggleActions(this)">
                        Ações ▼
                    </button>

                    <div class="actions-menu hidden">

                        <a onclick="abrirEdicaoCadastro('atendente', 'email', ${index})">
                            Editar e-mail
                        </a>

                        <a onclick="abrirEdicaoCadastro('atendente', 'senha', ${index})">
                            Editar senha
                        </a>

                        <a onclick="removerCadastro('atendente', ${index})">
                            Remover
                        </a>

                    </div>
                </div>
            </td>
        `;

        lista.appendChild(tr);
    });
}

// ===============================
// DROPDOWN AÇÕES
// ===============================

function toggleActions(button) {

    const menu =
        button.parentElement.querySelector(".actions-menu");

    if (!menu) return;

    menu.classList.toggle("hidden");
}

// ===============================
// SENHA OLHO
// ===============================

function toggleSenha(id, senhaReal) {

    const campo = document.getElementById(id);

    if (!campo) return;

    if (campo.textContent.trim() === "••••••") {

        campo.textContent = senhaReal;

    } else {

        campo.textContent = "••••••";
    }
}

function mostrarSenhaModal() {

    const input =
        document.getElementById("valorEditarCadastro");

    if (!input) return;

    if (input.type === "password") {

        input.type = "text";

    } else {

        input.type = "password";
    }
}

// ===============================
// MODAL USUÁRIO
// ===============================

let usuarioEditando = null;
let campoEditando = null;

function abrirEditarUsuario(campo, index) {

    usuarioEditando = index;
    campoEditando = campo;

    const modal =
        document.getElementById("modalEditarUsuario");

    const titulo =
        document.getElementById("tituloEditarUsuario");

    const input =
        document.getElementById("editUsuarioValor");

    const usuarios =
        JSON.parse(localStorage.getItem("usuariosCadastrados")) || [];

    const usuario = usuarios[index];

    titulo.textContent =
        `Editar ${campo}`;

    input.value =
        usuario[campo] || "";

    modal.classList.remove("hidden");
}

function fecharModalEditarUsuario() {

    document
        .getElementById("modalEditarUsuario")
        .classList.add("hidden");
}

function salvarEdicaoUsuario() {

    const usuarios =
        JSON.parse(localStorage.getItem("usuariosCadastrados")) || [];

    const valor =
        document.getElementById("editUsuarioValor").value.trim();

    let erro = "";

    if (campoEditando === "telefone") erro = Validacoes.telefone(valor);
    else if (campoEditando === "email") erro = Validacoes.email(valor);
    else if (campoEditando === "endereco") erro = Validacoes.enderecoUsuario(valor);

    if (erro) {
        alert(erro);
        return;
    }

    usuarios[usuarioEditando][campoEditando] = valor;

    localStorage.setItem(
        "usuariosCadastrados",
        JSON.stringify(usuarios)
    );

    fecharModalEditarUsuario();

    carregarUsuarios();

    abrirSucesso();
}

// ===============================
// MODAL INTERESSE
// ===============================

let interesseEditando = null;
let campoInteresse = null;

function abrirEditarInteresse(campo, index) {

    interesseEditando = index;
    campoInteresse = campo;

    const modal =
        document.getElementById("modalEditarInteresse");

    const titulo =
        document.getElementById("tituloEditarInteresse");

    const conteudo =
        document.getElementById("conteudoEditarInteresse");

    const interesses =
        JSON.parse(localStorage.getItem("interessesCadastrados")) || [];

    const item = interesses[index];

    const titulos = {
        status: "Editar status",
        tipo: "Editar tipo",
        descricao: "Editar descrição"
    };

    titulo.textContent = titulos[campo] || "Editar";

    if (campo === "status") {

        conteudo.innerHTML = `
            <select id="novoValorInteresse">
                <option value="Pendente" ${item.status === "Pendente" ? "selected" : ""}>Pendente</option>
                <option value="Em andamento" ${(item.status || "").toLowerCase().includes("andamento") ? "selected" : ""}>Em andamento</option>
                <option value="Concluído" ${(item.status || "").toLowerCase().includes("conclu") ? "selected" : ""}>Concluído</option>
            </select>
        `;

    } else if (campo === "tipo") {

        conteudo.innerHTML = `
            <select id="novoValorInteresse">
                <option value="doar" ${item.tipo === "doar" ? "selected" : ""}>Doar</option>
                <option value="receber" ${item.tipo === "receber" ? "selected" : ""}>Receber</option>
                <option value="ambos" ${item.tipo === "ambos" ? "selected" : ""}>Ambos</option>
            </select>
        `;

    } else {

        conteudo.innerHTML = `
            <input
                type="text"
                id="novoValorInteresse"
                value="${item.necessidade || ""}"
            >
        `;
    }

    modal.classList.remove("hidden");
}

function fecharModalEditarInteresse() {

    document
        .getElementById("modalEditarInteresse")
        .classList.add("hidden");
}

function salvarEdicaoInteresse() {

    const interesses =
        JSON.parse(localStorage.getItem("interessesCadastrados")) || [];

    const valor =
        document.getElementById("novoValorInteresse").value;

    if (campoInteresse === "descricao") {
        const erroDescricao = Validacoes.descricao(valor);

        if (erroDescricao) {
            alert(erroDescricao);
            return;
        }

        interesses[interesseEditando].necessidade = valor.trim();
    } else {
        interesses[interesseEditando][campoInteresse] = valor;
    }

    localStorage.setItem(
        "interessesCadastrados",
        JSON.stringify(interesses)
    );

    fecharModalEditarInteresse();

    carregarInteresses();

    abrirSucesso();
}

function removerInteresse(index) {

    const interesses =
        JSON.parse(localStorage.getItem("interessesCadastrados")) || [];

    interesses.splice(index, 1);

    localStorage.setItem(
        "interessesCadastrados",
        JSON.stringify(interesses)
    );

    carregarInteresses();
}

// ===============================
// ADMIN / ATENDENTE
// ===============================

let tipoCadastro = null;
let cadastroIndex = null;
let campoCadastro = null;

function abrirEdicaoCadastro(tipo, campo, index) {

    tipoCadastro = tipo;
    cadastroIndex = index;
    campoCadastro = campo;

    const modal =
        document.getElementById("modalEditarCadastro");

    const titulo =
        document.getElementById("tituloEditarCadastro");

    const input =
        document.getElementById("valorEditarCadastro");

    const lista =
        JSON.parse(localStorage.getItem(
            tipo === "admin"
                ? "administradores"
                : "atendentes"
        )) || [];

    const item = lista[index];

    if (campo === "email") {

        titulo.textContent = "Editar e-mail";

        input.placeholder = "Novo e-mail";

        input.value = item.email || "";

        input.type = "email";
    }

    if (campo === "senha") {

        titulo.textContent = "Editar senha";

        input.placeholder = "Nova senha";

        input.value = item.senha || "";

        input.type = "password";
    }

    modal.classList.remove("hidden");
}

function fecharModalEditarCadastro() {

    document
        .getElementById("modalEditarCadastro")
        .classList.add("hidden");
}

function salvarEdicaoCadastro() {

    const chave =
        tipoCadastro === "admin"
            ? "administradores"
            : "atendentes";

    const lista =
        JSON.parse(localStorage.getItem(chave)) || [];

    const valorBruto =
        document.getElementById("valorEditarCadastro").value;

    const valorNovo =
        campoCadastro === "email" ? valorBruto.trim() : valorBruto;

    let erro = "";

    if (campoCadastro === "email") {
        erro =
            Validacoes.email(valorNovo) ||
            (Validacoes.emailJaCadastrado(chave, valorNovo, lista[cadastroIndex].email)
                ? "Email já cadastrado."
                : "");
    } else if (campoCadastro === "senha") {
        erro = Validacoes.senha(valorBruto);
    }

    if (erro) {
        alert(erro);
        return;
    }

    lista[cadastroIndex][campoCadastro] = valorNovo;

    localStorage.setItem(
        chave,
        JSON.stringify(lista)
    );

    fecharModalEditarCadastro();

    carregarAdministradores();

    carregarAtendentes();

    abrirSucesso();
}

function removerCadastro(tipo, index) {

    const chave =
        tipo === "admin"
            ? "administradores"
            : "atendentes";

    const lista =
        JSON.parse(localStorage.getItem(chave)) || [];

    lista.splice(index, 1);

    localStorage.setItem(
        chave,
        JSON.stringify(lista)
    );

    carregarAdministradores();

    carregarAtendentes();
}

// ===============================
// SUCESSO
// ===============================

function abrirSucesso() {

    document
        .getElementById("sucessoModal")
        .classList.remove("hidden");
}

function fecharSucesso() {

    document
        .getElementById("sucessoModal")
        .classList.add("hidden");
}

// ===============================
// PESQUISA
// ===============================

function configurarPesquisas() {

    pesquisar(
        "searchUsuarios",
        "listaUsuarios"
    );

    pesquisar(
        "searchInteresses",
        "listaInteresses"
    );

    pesquisar(
        "searchAdministradores",
        "listaAdministradores"
    );

    pesquisar(
        "searchAtendentes",
        "listaAtendentes"
    );
}

function pesquisar(inputId, tabelaId) {

    const input =
        document.getElementById(inputId);

    const tabela =
        document.getElementById(tabelaId);

    if (!input || !tabela) return;

    input.addEventListener("keyup", () => {

        const valor =
            input.value.toLowerCase();

        const linhas =
            tabela.querySelectorAll("tr");

        linhas.forEach(linha => {

            const texto =
                linha.textContent.toLowerCase();

            linha.style.display =
                texto.includes(valor)
                    ? ""
                    : "none";
        });
    });
}
// ===============================
// CRIAR ADMIN
// ===============================

function abrirCriarAdmin() {

    const modal =
        document.getElementById("modalCriarUsuario");

    const titulo =
        document.getElementById("tituloCriarUsuario");

    titulo.textContent = "Criar Administrador";

    modal.classList.remove("hidden");

    modal.dataset.tipo = "admin";
}

// ===============================
// CRIAR ATENDENTE
// ===============================

function abrirCriarAtendente() {

    const modal =
        document.getElementById("modalCriarUsuario");

    const titulo =
        document.getElementById("tituloCriarUsuario");

    titulo.textContent = "Criar Atendente";

    modal.classList.remove("hidden");

    modal.dataset.tipo = "atendente";
}

// ===============================
// FECHAR MODAL
// ===============================

function fecharModalCriarUsuario() {

    document
        .getElementById("modalCriarUsuario")
        .classList.add("hidden");
}

// ===============================
// SALVAR NOVO USUÁRIO
// ===============================

function salvarNovoUsuario() {

    const nome =
        document.getElementById("novoNomeUsuario").value;

    const email =
        document.getElementById("novoEmailUsuario").value;

    const senha =
        document.getElementById("novaSenhaUsuario").value;

    const modal =
        document.getElementById("modalCriarUsuario");

    const tipo =
        modal.dataset.tipo;

    const chave =
        tipo === "admin"
            ? "administradores"
            : "atendentes";

    const erro =
        Validacoes.nome(nome) ||
        Validacoes.email(email) ||
        (Validacoes.emailJaCadastrado(chave, email, "") ? "Email já cadastrado." : "") ||
        Validacoes.senha(senha);

    if (erro) {
        alert(erro);
        return;
    }

    const lista =
        JSON.parse(localStorage.getItem(chave)) || [];

    lista.push({
        nome: nome.trim(),
        email: email.trim(),
        senha
    });

    localStorage.setItem(
        chave,
        JSON.stringify(lista)
    );

    fecharModalCriarUsuario();

    carregarAdministradores();

    carregarAtendentes();

    abrirSucesso();
}
// ===============================
// DADOS ONG
// ===============================

function editarONG(campo, botao) {
    const campos = {
        telefone: {
            texto: "ongTelefoneText",
            input: "ongTelefoneInput",
            chave: "ongTelefone"
        },
        email: {
            texto: "ongEmailText",
            input: "ongEmailInput",
            chave: "ongEmail"
        },
        endereco: {
            texto: "ongEnderecoText",
            input: "ongEnderecoInput",
            chave: "ongEndereco"
        }
    };

    const alvo = campos[campo];
    if (!alvo) return;

    const span = document.getElementById(alvo.texto);
    const input = document.getElementById(alvo.input);

    if (!span || !input) return;

    const estaEditando = !input.classList.contains("hidden-input");

    if (!estaEditando) {

        /* entra no modo de edição */
        span.classList.add("hidden-input");
        input.classList.remove("hidden-input");
        input.focus();

        if (botao) {
            botao.textContent = "Salvar";
            botao.classList.add("btn-salvar-ong");
        }

    } else {

        /* salva só esse campo */
        const valor = input.value.trim();

        let erro = "";

        if (campo === "telefone") erro = Validacoes.telefone(valor);
        else if (campo === "email") erro = Validacoes.email(valor);
        else if (campo === "endereco") erro = Validacoes.enderecoOng(valor);

        if (erro) {
            alert(erro);
            input.focus();
            return;
        }

        localStorage.setItem(alvo.chave, valor);

        span.textContent = valor;
        span.classList.remove("hidden-input");
        input.classList.add("hidden-input");

        if (botao) {
            botao.textContent = "Editar";
            botao.classList.remove("btn-salvar-ong");
        }

        abrirSucesso();
    }
}

/* cancela a edição de um campo da ONG sem salvar
   (usado quando a pessoa clica fora do campo que estava editando) */
function cancelarEdicaoONG(campo, botao) {
    const campos = {
        telefone: { texto: "ongTelefoneText", input: "ongTelefoneInput" },
        email: { texto: "ongEmailText", input: "ongEmailInput" },
        endereco: { texto: "ongEnderecoText", input: "ongEnderecoInput" }
    };

    const alvo = campos[campo];
    if (!alvo) return;

    const span = document.getElementById(alvo.texto);
    const input = document.getElementById(alvo.input);

    if (!span || !input) return;

    /* devolve o valor original (desfaz o que a pessoa digitou) */
    input.value = span.textContent;

    span.classList.remove("hidden-input");
    input.classList.add("hidden-input");

    if (botao) {
        botao.textContent = "Editar";
        botao.classList.remove("btn-salvar-ong");
    }
}

function salvarDadosONG() {
    const telefone = document.getElementById("ongTelefoneInput").value.trim();
    const email = document.getElementById("ongEmailInput").value.trim();
    const endereco = document.getElementById("ongEnderecoInput").value.trim();

    localStorage.setItem("ongTelefone", telefone);
    localStorage.setItem("ongEmail", email);
    localStorage.setItem("ongEndereco", endereco);

    document.getElementById("ongTelefoneText").textContent = telefone;
    document.getElementById("ongEmailText").textContent = email;
    document.getElementById("ongEnderecoText").textContent = endereco;

    document.getElementById("ongTelefoneText").classList.remove("hidden-input");
    document.getElementById("ongEmailText").classList.remove("hidden-input");
    document.getElementById("ongEnderecoText").classList.remove("hidden-input");

    document.getElementById("ongTelefoneInput").classList.add("hidden-input");
    document.getElementById("ongEmailInput").classList.add("hidden-input");
    document.getElementById("ongEnderecoInput").classList.add("hidden-input");

    alert("Dados da ONG salvos com sucesso!");
}

function carregarDadosONG() {
    const telefone = localStorage.getItem("ongTelefone");
    const email = localStorage.getItem("ongEmail");
    const endereco = localStorage.getItem("ongEndereco");

    if (telefone) {
        document.getElementById("ongTelefoneText").textContent = telefone;
        document.getElementById("ongTelefoneInput").value = telefone;
    }

    if (email) {
        document.getElementById("ongEmailText").textContent = email;
        document.getElementById("ongEmailInput").value = email;
    }

    if (endereco) {
        document.getElementById("ongEnderecoText").textContent = endereco;
        document.getElementById("ongEnderecoInput").value = endereco;
    }
}

let campoPerfilEditando = "";

function editarPerfil(campo) {
    campoPerfilEditando = campo;

    const titulos = {
        nome: "Editar nome",
        email: "Editar email",
        celular: "Editar celular"
    };

    const spans = {
        nome: "nomeText",
        email: "emailText",
        celular: "celularText"
    };

    const inputs = {
        nome: "nomeInput",
        email: "emailInput",
        celular: "celularInput"
    };

    const modal = document.getElementById("modalEditarPerfil");
    const titulo = document.getElementById("tituloEditarPerfil");
    const input = document.getElementById("valorEditarPerfil");

    if (!modal || !titulo || !input) return;

    titulo.textContent = titulos[campo];
    input.value = document.getElementById(spans[campo]).textContent.trim();
    input.placeholder = titulos[campo];

    modal.classList.remove("hidden");
}

function fecharEditarPerfil() {
    document.getElementById("modalEditarPerfil").classList.add("hidden");
}

function salvarEditarPerfil() {
    const valor = document.getElementById("valorEditarPerfil").value.trim();

    if (!valor) {
        alert("Preencha o campo.");
        return;
    }

    /* descobre em qual lista (administradores/atendentes) está o registro
       de login correspondente à conta que está editando o perfil agora */
    const tipoUsuario = localStorage.getItem("tipoUsuario");
    const chave = tipoUsuario === "atendente" ? "atendentes" : "administradores";
    const emailAtual = (localStorage.getItem("adminEmail") || "").trim().toLowerCase();

    const lista = JSON.parse(localStorage.getItem(chave)) || [];
    const indice = lista.findIndex(
        item => (item.email || "").trim().toLowerCase() === emailAtual
    );

    let erroPerfil = "";

    if (campoPerfilEditando === "nome") {
        erroPerfil = Validacoes.nome(valor);
    } else if (campoPerfilEditando === "email") {
        erroPerfil =
            Validacoes.email(valor) ||
            (Validacoes.emailJaCadastrado(chave, valor, emailAtual)
                ? "Email já cadastrado."
                : "");
    } else if (campoPerfilEditando === "celular") {
        erroPerfil = Validacoes.telefone(valor);
    }

    if (erroPerfil) {
        alert(erroPerfil);
        return;
    }

    if (campoPerfilEditando === "nome") {
        document.getElementById("nomeText").textContent = valor;
        localStorage.setItem("adminNome", valor);

        const nomeDropdown = document.getElementById("adminNomeText");
        if (nomeDropdown) {
            nomeDropdown.textContent = valor;
        }

        if (indice !== -1) {
            lista[indice].nome = valor;
            localStorage.setItem(chave, JSON.stringify(lista));
        }
    }

    if (campoPerfilEditando === "email") {
        document.getElementById("emailText").textContent = valor;

        if (indice !== -1) {
            lista[indice].email = valor;
            localStorage.setItem(chave, JSON.stringify(lista));
        }

        localStorage.setItem("adminEmail", valor);
    }

    if (campoPerfilEditando === "celular") {
        document.getElementById("celularText").textContent = valor;
        localStorage.setItem("adminCelular", valor);
    }

    document.getElementById("modalEditarPerfil").classList.add("hidden");

    if (indice === -1 && (campoPerfilEditando === "nome" || campoPerfilEditando === "email")) {
        alert("Salvo! (Essa conta é o login padrão, não uma criada pelo painel — então isso não muda o e-mail usado para entrar.)");
    } else {
        alert("Salvo com sucesso!");
    }
}

function carregarPerfil() {
    const nome = localStorage.getItem("adminNome");
    const email = localStorage.getItem("adminEmail");
    const celular = localStorage.getItem("adminCelular");

    if (nome) document.getElementById("nomeText").textContent = nome;
    if (email) document.getElementById("emailText").textContent = email;
    if (celular) document.getElementById("celularText").textContent = celular;
}
function sairPerfil() {
    localStorage.removeItem("tipoUsuario");
    localStorage.removeItem("adminEmail");
    localStorage.removeItem("adminNome");
    window.location.href = "index.html";
}


// ===============================
// ENTER PARA CONFIRMAR / CLIQUE FORA PARA FECHAR
// ===============================

/* mapeia cada input/select de dentro de um modal para o botão
   "Salvar" correspondente, pra pressionar Enter disparar a mesma ação */
document.addEventListener("keydown", (evento) => {

    if (evento.key !== "Enter") return;

    const alvo = evento.target;

    if (alvo.tagName === "TEXTAREA") return;

    const modal = alvo.closest(".modal:not(.hidden)");

    if (!modal) return;

    const botaoSalvar = modal.querySelector(".btn-salvar");

    if (botaoSalvar) {
        evento.preventDefault();
        botaoSalvar.click();
    }
});

/* clicar fora de um menu/caixa aberta fecha ela */
document.addEventListener("click", (evento) => {

    // dropdown de perfil (topo direito)
    const dropdown = document.getElementById("profileDropdown");
    const botaoPerfil = document.querySelector(".profile-btn");

    if (dropdown && !dropdown.classList.contains("hidden")) {
        if (!dropdown.contains(evento.target) && evento.target !== botaoPerfil) {
            dropdown.classList.add("hidden");
        }
    }

    // menus "Ações ▼" das tabelas
    document.querySelectorAll(".actions-menu:not(.hidden)").forEach((menu) => {
        const container = menu.closest(".dropdown-actions");
        if (container && !container.contains(evento.target)) {
            menu.classList.add("hidden");
        }
    });

    // modais (clicar no fundo escuro fecha; clicar dentro do card não)
    document.querySelectorAll(".modal:not(.hidden)").forEach((modal) => {
        if (evento.target === modal) {
            modal.classList.add("hidden");
        }
    });

    // campos da ONG que estão sendo editados (Telefone/Email/Endereço)
    const camposONG = {
        ongTelefoneInput: "telefone",
        ongEmailInput: "email",
        ongEnderecoInput: "endereco"
    };

    Object.keys(camposONG).forEach((idInput) => {
        const input = document.getElementById(idInput);
        if (!input) return;

        const emEdicao = !input.classList.contains("hidden-input");
        if (!emEdicao) return;

        const linha = input.closest(".info");
        if (linha && !linha.contains(evento.target)) {
            const botao = linha.querySelector("button");
            cancelarEdicaoONG(camposONG[idInput], botao);
        }
    });
});
