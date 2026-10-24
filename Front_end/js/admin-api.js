/* ============================================================
   PAINEL DO ADMIN LIGADO AO BANCO (via API em http://localhost:5000)

   Este arquivo vem DEPOIS do admin.js no admin.html. As funções
   abaixo têm o mesmo nome das antigas (que usavam localStorage),
   então a página passa a usar estas, que falam com a API.
============================================================ */

const API_PAINEL = "http://localhost:5000";

/* guarda o que veio da API (a tela usa a posição da linha) */
let usuariosApi = [];
let interessesApi = [];
let administradoresApi = [];
let atendentesApi = [];

/* ---------- apoio ---------- */

function esc(texto) {
    return String(texto == null ? "" : texto)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

function formatarCpf(cpf) {
    const d = (cpf || "").replace(/\D/g, "");
    if (d.length !== 11) return cpf || "";
    return d.slice(0, 3) + "." + d.slice(3, 6) + "." + d.slice(6, 9) + "-" + d.slice(9);
}

function formatarTelefone(tel) {
    const d = (tel || "").replace(/\D/g, "");
    if (d.length === 11) return "(" + d.slice(0, 2) + ") " + d.slice(2, 7) + "-" + d.slice(7);
    if (d.length === 10) return "(" + d.slice(0, 2) + ") " + d.slice(2, 6) + "-" + d.slice(6);
    return tel || "";
}

/* chama a API e devolve { ok, dados }; em caso de erro de rede avisa a pessoa */
async function chamarApi(metodo, caminho, corpo) {
    try {
        const opcoes = { method: metodo, headers: {} };
        if (corpo !== undefined) {
            opcoes.headers["Content-Type"] = "application/json";
            opcoes.body = JSON.stringify(corpo);
        }
        const resposta = await fetch(API_PAINEL + caminho, opcoes);
        let dados = null;
        try { dados = await resposta.json(); } catch (e) { dados = null; }
        return { ok: resposta.ok, status: resposta.status, dados: dados };
    } catch (e) {
        alert("Não foi possível falar com o servidor. Confira se a API está ligada.");
        return { ok: false, status: 0, dados: null };
    }
}

function mensagemErro(resultado, padrao) {
    if (resultado && resultado.dados && resultado.dados.erro) return resultado.dados.erro;
    return padrao;
}

/* ============================================================
   USUÁRIOS
============================================================ */

async function carregarUsuarios() {
    const lista = document.getElementById("listaUsuarios");
    if (!lista) return;

    const r = await chamarApi("GET", "/usuarios");
    usuariosApi = r.ok && Array.isArray(r.dados) ? r.dados : [];

    lista.innerHTML = "";

    usuariosApi.forEach((usuario, index) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${esc(usuario.nome)}</td>
            <td>${esc(formatarCpf(usuario.cpf))}</td>
            <td>${esc(usuario.email)}</td>
            <td>${esc(formatarTelefone(usuario.telefone))}</td>
            <td>${esc(usuario.endereco)}</td>
            <td>
                <div class="dropdown-actions">
                    <button onclick="toggleActions(this)">Ações ▼</button>
                    <div class="actions-menu hidden">
                        <a onclick="abrirEditarUsuario('telefone', ${index})">Editar telefone</a>
                        <a onclick="abrirEditarUsuario('email', ${index})">Editar e-mail</a>
                        <a onclick="abrirEditarUsuario('endereco', ${index})">Editar endereço</a>
                        <a onclick="removerUsuario(${index})">Remover</a>
                    </div>
                </div>
            </td>
        `;
        lista.appendChild(tr);
    });
}

async function removerUsuario(index) {
    const usuario = usuariosApi[index];
    if (!usuario) return;

    if (!confirm("Remover " + usuario.nome + "? Os interesses dele também serão apagados.")) return;

    const r = await chamarApi("DELETE", "/usuarios/" + usuario.idUsuario);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível remover."));
        return;
    }
    carregarUsuarios();
    carregarInteresses();
}

function abrirEditarUsuario(campo, index) {
    usuarioEditando = index;
    campoEditando = campo;

    const usuario = usuariosApi[index];
    if (!usuario) return;

    document.getElementById("tituloEditarUsuario").textContent = "Editar " + campo;
    document.getElementById("editUsuarioValor").value = usuario[campo] || "";
    document.getElementById("modalEditarUsuario").classList.remove("hidden");
}

async function salvarEdicaoUsuario() {
    const usuario = usuariosApi[usuarioEditando];
    if (!usuario) return;

    const valor = document.getElementById("editUsuarioValor").value.trim();

    let erro = "";
    if (campoEditando === "telefone") erro = Validacoes.telefone(valor);
    else if (campoEditando === "email") erro = Validacoes.email(valor);
    else if (campoEditando === "endereco") erro = Validacoes.enderecoUsuario(valor);

    if (erro) {
        alert(erro);
        return;
    }

    const corpo = {};
    corpo[campoEditando] = valor;

    const r = await chamarApi("PUT", "/usuarios/" + usuario.idUsuario, corpo);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível salvar."));
        return;
    }

    fecharModalEditarUsuario();
    carregarUsuarios();
    abrirSucesso();
}

/* ============================================================
   INTERESSES
============================================================ */

async function carregarInteresses() {
    const lista = document.getElementById("listaInteresses");
    if (!lista) return;

    const r = await chamarApi("GET", "/interesses");
    interessesApi = r.ok && Array.isArray(r.dados) ? r.dados : [];

    lista.innerHTML = "";

    interessesApi.forEach((item, index) => {
        const statusTexto = item.status || "Pendente";
        const statusLower = statusTexto.toLowerCase();
        const classeStatus =
            statusLower.includes("andamento") ? "andamento" :
                statusLower.includes("conclu") ? "concluido" : "pendente";

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${esc(item.nome)}</td>
            <td><span class="tipo ${esc(item.tipo)}">${esc(item.tipo)}</span></td>
            <td>${esc(item.necessidade)}</td>
            <td><span class="${classeStatus}">${esc(statusTexto)}</span></td>
            <td>${esc(item.dataSolicitacao)}</td>
            <td>
                <div class="dropdown-actions">
                    <button onclick="toggleActions(this)">Ações ▼</button>
                    <div class="actions-menu hidden">
                        <a onclick="abrirEditarInteresse('status', ${index})">Editar status</a>
                        <a onclick="abrirEditarInteresse('descricao', ${index})">Editar descrição</a>
                        <a onclick="abrirEditarInteresse('tipo', ${index})">Editar tipo</a>
                        <a onclick="removerInteresse(${index})">Remover</a>
                    </div>
                </div>
            </td>
        `;
        lista.appendChild(tr);
    });
}

function abrirEditarInteresse(campo, index) {
    interesseEditando = index;
    campoInteresse = campo;

    const item = interessesApi[index];
    if (!item) return;

    const titulos = {
        status: "Editar status",
        tipo: "Editar tipo",
        descricao: "Editar descrição"
    };
    document.getElementById("tituloEditarInteresse").textContent = titulos[campo] || "Editar";

    const conteudo = document.getElementById("conteudoEditarInteresse");
    const statusLower = (item.status || "").toLowerCase();

    if (campo === "status") {
        conteudo.innerHTML = `
            <select id="novoValorInteresse">
                <option value="Pendente" ${statusLower.includes("pendente") ? "selected" : ""}>Pendente</option>
                <option value="Em andamento" ${statusLower.includes("andamento") ? "selected" : ""}>Em andamento</option>
                <option value="Concluído" ${statusLower.includes("conclu") ? "selected" : ""}>Concluído</option>
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
            <input type="text" id="novoValorInteresse" value="${esc(item.necessidade)}">
        `;
    }

    document.getElementById("modalEditarInteresse").classList.remove("hidden");
}

async function salvarEdicaoInteresse() {
    const item = interessesApi[interesseEditando];
    if (!item) return;

    const valor = document.getElementById("novoValorInteresse").value;

    if (campoInteresse === "descricao") {
        const erroDescricao = Validacoes.descricao(valor);
        if (erroDescricao) {
            alert(erroDescricao);
            return;
        }
    }

    const corpo = {};
    corpo[campoInteresse] = campoInteresse === "descricao" ? valor.trim() : valor;

    const r = await chamarApi("PUT", "/interesses/" + item.id, corpo);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível salvar."));
        return;
    }

    fecharModalEditarInteresse();
    carregarInteresses();
    abrirSucesso();
}

async function removerInteresse(index) {
    const item = interessesApi[index];
    if (!item) return;

    if (!confirm("Remover este interesse?")) return;

    const r = await chamarApi("DELETE", "/interesses/" + item.id);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível remover."));
        return;
    }
    carregarInteresses();
}

/* ============================================================
   ADMINISTRADORES E ATENDENTES
============================================================ */

function linhaConta(conta, tipo, index) {
    return `
        <tr>
            <td>${esc(conta.nome)}</td>
            <td>${esc(conta.email)}</td>
            <td>
                <div class="senha-box">
                    <span>••••••</span>
                </div>
            </td>
            <td>
                <div class="dropdown-actions">
                    <button onclick="toggleActions(this)">Ações ▼</button>
                    <div class="actions-menu hidden">
                        <a onclick="abrirEdicaoCadastro('${tipo}', 'email', ${index})">Editar e-mail</a>
                        <a onclick="abrirEdicaoCadastro('${tipo}', 'senha', ${index})">Editar senha</a>
                        <a onclick="removerCadastro('${tipo}', ${index})">Remover</a>
                    </div>
                </div>
            </td>
        </tr>
    `;
}

async function carregarAdministradores() {
    const lista = document.getElementById("listaAdministradores");
    if (!lista) return;

    const r = await chamarApi("GET", "/administradores");
    administradoresApi = r.ok && Array.isArray(r.dados) ? r.dados : [];

    lista.innerHTML = administradoresApi.map((a, i) => linhaConta(a, "admin", i)).join("");
}

async function carregarAtendentes() {
    const lista = document.getElementById("listaAtendentes");
    if (!lista) return;

    const r = await chamarApi("GET", "/atendentes");
    atendentesApi = r.ok && Array.isArray(r.dados) ? r.dados : [];

    lista.innerHTML = atendentesApi.map((a, i) => linhaConta(a, "atendente", i)).join("");
}

function rotaConta(tipo) {
    return tipo === "admin" ? "/administradores" : "/atendentes";
}

function abrirEdicaoCadastro(tipo, campo, index) {
    tipoCadastro = tipo;
    cadastroIndex = index;
    campoCadastro = campo;

    const lista = tipo === "admin" ? administradoresApi : atendentesApi;
    const item = lista[index];
    if (!item) return;

    const titulo = document.getElementById("tituloEditarCadastro");
    const input = document.getElementById("valorEditarCadastro");

    if (campo === "email") {
        titulo.textContent = "Editar e-mail";
        input.placeholder = "Novo e-mail";
        input.value = item.email || "";
        input.type = "email";
    }

    if (campo === "senha") {
        titulo.textContent = "Editar senha";
        input.placeholder = "Nova senha";
        input.value = "";
        input.type = "password";
    }

    document.getElementById("modalEditarCadastro").classList.remove("hidden");
}

async function salvarEdicaoCadastro() {
    const lista = tipoCadastro === "admin" ? administradoresApi : atendentesApi;
    const item = lista[cadastroIndex];
    if (!item) return;

    const valorBruto = document.getElementById("valorEditarCadastro").value;
    const valorNovo = campoCadastro === "email" ? valorBruto.trim() : valorBruto;

    let erro = "";
    if (campoCadastro === "email") erro = Validacoes.email(valorNovo);
    else if (campoCadastro === "senha") erro = Validacoes.senha(valorBruto);

    if (erro) {
        alert(erro);
        return;
    }

    const corpo = {};
    corpo[campoCadastro] = valorNovo;

    const r = await chamarApi(
        "PUT",
        rotaConta(tipoCadastro) + "?email=" + encodeURIComponent(item.email),
        corpo
    );
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível salvar."));
        return;
    }

    fecharModalEditarCadastro();
    carregarAdministradores();
    carregarAtendentes();
    abrirSucesso();
}

async function removerCadastro(tipo, index) {
    const lista = tipo === "admin" ? administradoresApi : atendentesApi;
    const item = lista[index];
    if (!item) return;

    if (!confirm("Remover " + item.nome + "?")) return;

    const r = await chamarApi("DELETE", rotaConta(tipo) + "?email=" + encodeURIComponent(item.email));
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível remover."));
        return;
    }

    carregarAdministradores();
    carregarAtendentes();
}

async function salvarNovoUsuario() {
    const nome = document.getElementById("novoNomeUsuario").value;
    const email = document.getElementById("novoEmailUsuario").value;
    const senha = document.getElementById("novaSenhaUsuario").value;
    const modal = document.getElementById("modalCriarUsuario");
    const tipo = modal.dataset.tipo;

    const erro =
        Validacoes.nome(nome) ||
        Validacoes.email(email) ||
        Validacoes.senha(senha);

    if (erro) {
        alert(erro);
        return;
    }

    const r = await chamarApi("POST", rotaConta(tipo), {
        nome: nome.trim(),
        email: email.trim(),
        senha: senha
    });
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível criar."));
        return;
    }

    fecharModalCriarUsuario();
    carregarAdministradores();
    carregarAtendentes();
    abrirSucesso();
}

/* ============================================================
   DADOS DA ONG
============================================================ */

async function carregarDadosONG() {
    const r = await chamarApi("GET", "/ong");
    if (!r.ok || !r.dados) return;

    const dados = {
        Telefone: formatarTelefone(r.dados.telefone),
        Email: r.dados.email || "",
        Endereco: r.dados.endereco || ""
    };

    Object.keys(dados).forEach((campo) => {
        const texto = document.getElementById("ong" + campo + "Text");
        const input = document.getElementById("ong" + campo + "Input");
        if (texto) texto.textContent = dados[campo];
        if (input) input.value = dados[campo];
    });
}

async function editarONG(campo, botao) {
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

    const estaEditando = !input.classList.contains("hidden-input");

    if (!estaEditando) {
        span.classList.add("hidden-input");
        input.classList.remove("hidden-input");
        input.focus();

        if (botao) {
            botao.textContent = "Salvar";
            botao.classList.add("btn-salvar-ong");
        }
        return;
    }

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

    const corpo = {};
    corpo[campo] = valor;

    const r = await chamarApi("PUT", "/ong", corpo);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível salvar."));
        return;
    }

    span.textContent = campo === "telefone" ? formatarTelefone(valor) : valor;
    span.classList.remove("hidden-input");
    input.classList.add("hidden-input");

    if (botao) {
        botao.textContent = "Editar";
        botao.classList.remove("btn-salvar-ong");
    }

    abrirSucesso();
}

/* ============================================================
   PERFIL (quem está logado: administrador ou atendente)
============================================================ */

function rotaPerfil() {
    return localStorage.getItem("tipoUsuario") === "atendente" ? "/atendentes" : "/administradores";
}

async function salvarEditarPerfil() {
    const valor = document.getElementById("valorEditarPerfil").value.trim();

    if (!valor) {
        alert("Preencha o campo.");
        return;
    }

    const emailAtual = (localStorage.getItem("adminEmail") || "").trim();

    let erroPerfil = "";
    if (campoPerfilEditando === "nome") erroPerfil = Validacoes.nome(valor);
    else if (campoPerfilEditando === "email") erroPerfil = Validacoes.email(valor);
    else if (campoPerfilEditando === "celular") erroPerfil = Validacoes.telefone(valor);

    if (erroPerfil) {
        alert(erroPerfil);
        return;
    }

    const corpo = {};
    if (campoPerfilEditando === "nome") corpo.nome = valor;
    if (campoPerfilEditando === "email") corpo.email = valor;
    if (campoPerfilEditando === "celular") corpo.telefone = valor;

    const r = await chamarApi("PUT", rotaPerfil() + "?email=" + encodeURIComponent(emailAtual), corpo);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível salvar."));
        return;
    }

    if (campoPerfilEditando === "nome") {
        document.getElementById("nomeText").textContent = valor;
        localStorage.setItem("adminNome", valor);
        const nomeDropdown = document.getElementById("adminNomeText");
        if (nomeDropdown) nomeDropdown.textContent = valor;
    }

    if (campoPerfilEditando === "email") {
        document.getElementById("emailText").textContent = valor;
        localStorage.setItem("adminEmail", valor);
        if (localStorage.getItem("tipoUsuario") === "atendente") {
            localStorage.setItem("atendenteEmail", valor);
        }
        const emailDropdown = document.getElementById("adminEmailText");
        if (emailDropdown) emailDropdown.textContent = valor;
    }

    if (campoPerfilEditando === "celular") {
        document.getElementById("celularText").textContent = formatarTelefone(valor);
        localStorage.setItem("adminCelular", valor);
    }

    document.getElementById("modalEditarPerfil").classList.add("hidden");
    alert("Salvo com sucesso!");
}

async function carregarPerfil() {
    const nome = localStorage.getItem("adminNome");
    const email = localStorage.getItem("adminEmail");

    if (nome) document.getElementById("nomeText").textContent = nome;
    if (email) document.getElementById("emailText").textContent = email;

    /* o celular do administrador vem do banco */
    const celularEl = document.getElementById("celularText");
    if (!celularEl) return;

    if (localStorage.getItem("tipoUsuario") !== "atendente") {
        const r = await chamarApi("GET", "/administradores");
        if (r.ok && Array.isArray(r.dados)) {
            const eu = r.dados.find(a => (a.email || "").toLowerCase() === (email || "").toLowerCase());
            if (eu && eu.telefone) {
                celularEl.textContent = formatarTelefone(eu.telefone);
                return;
            }
        }
    }

    const celularLocal = localStorage.getItem("adminCelular");
    if (celularLocal) celularEl.textContent = formatarTelefone(celularLocal);
}
