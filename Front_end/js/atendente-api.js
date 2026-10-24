/* ============================================================
   PAINEL DO ATENDENTE LIGADO AO BANCO (via API em http://localhost:5000)

   Este arquivo vem DEPOIS do script da página (no atendente.html).
   As funções abaixo têm o mesmo nome das antigas (que usavam
   localStorage), então a página passa a usar estas.
============================================================ */

const API_ATENDENTE = "http://localhost:5000";

let usuariosAtd = [];
let interessesAtd = [];

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

async function chamarApi(metodo, caminho, corpo) {
    try {
        const opcoes = { method: metodo, headers: {} };
        if (corpo !== undefined) {
            opcoes.headers["Content-Type"] = "application/json";
            opcoes.body = JSON.stringify(corpo);
        }
        const resposta = await fetch(API_ATENDENTE + caminho, opcoes);
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

async function carregarUsuariosAtendente() {
    const tbody = document.getElementById("listaUsuariosAtendente");
    if (!tbody) return;

    const r = await chamarApi("GET", "/usuarios");
    usuariosAtd = r.ok && Array.isArray(r.dados) ? r.dados : [];

    tbody.innerHTML = "";

    usuariosAtd.forEach((usuario, index) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${esc(usuario.nome)}</td>
            <td>${esc(formatarCpf(usuario.cpf))}</td>
            <td>${esc(usuario.email)}</td>
            <td>${esc(formatarTelefone(usuario.telefone))}</td>
            <td>${esc(usuario.endereco)}</td>
            <td>
                <div class="dropdown-actions">
                    <button class="btn-acoes" type="button" onclick="toggleActions(this)">Ações ▼</button>
                    <div class="actions-menu hidden">
                        <button type="button" onclick="abrirModalUsuario('telefone', ${index})">Editar telefone</button>
                        <button type="button" onclick="abrirModalUsuario('email', ${index})">Editar email</button>
                        <button type="button" onclick="abrirModalUsuario('endereco', ${index})">Editar endereço</button>
                    </div>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function abrirModalUsuario(campo, index) {
    usuarioEditandoIndex = index;
    usuarioEditandoCampo = campo;

    const usuario = usuariosAtd[index] || {};

    const titulos = {
        telefone: "Editar telefone",
        email: "Editar email",
        endereco: "Editar endereço"
    };

    document.getElementById("tituloEditarUsuario").textContent = titulos[campo] || "Editar";
    document.getElementById("valorEditarUsuario").value = usuario[campo] || "";
    document.getElementById("modalEditarUsuario").classList.remove("hidden");
}

async function salvarEdicaoUsuario() {
    const valor = document.getElementById("valorEditarUsuario").value.trim();

    if (!valor) {
        alert("Preencha o campo.");
        return;
    }

    const erro =
        usuarioEditandoCampo === "telefone" ? Validacoes.telefone(valor) :
            usuarioEditandoCampo === "email" ? Validacoes.email(valor) :
                usuarioEditandoCampo === "endereco" ? Validacoes.enderecoUsuario(valor) : "";

    if (erro) {
        alert(erro);
        return;
    }

    const usuario = usuariosAtd[usuarioEditandoIndex];
    if (!usuario) return;

    const corpo = {};
    corpo[usuarioEditandoCampo] = valor;

    const r = await chamarApi("PUT", "/usuarios/" + usuario.idUsuario, corpo);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível salvar."));
        return;
    }

    fecharModalUsuario();
    carregarUsuariosAtendente();
    abrirSucessoAtendente();
}

/* ============================================================
   INTERESSES
============================================================ */

async function carregarInteressesAtendente() {
    const tbody = document.getElementById("listaInteressesAtendente");
    if (!tbody) return;

    const r = await chamarApi("GET", "/interesses");
    interessesAtd = r.ok && Array.isArray(r.dados) ? r.dados : [];

    tbody.innerHTML = "";

    interessesAtd.forEach((item, index) => {
        const tipo = (item.tipo || "").toLowerCase();
        const classeTipo =
            tipo === "doar" ? "doar" :
                tipo === "receber" ? "receber" : "ambos";

        const statusTexto = item.status || "Pendente";
        const status = statusTexto.toLowerCase();
        const classeStatus =
            status.includes("andamento") ? "andamento" :
                status.includes("conclu") ? "concluido" : "pendente";

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${esc(item.nome)}</td>
            <td><span class="tipo ${classeTipo}">${esc(item.tipo)}</span></td>
            <td>${esc(item.necessidade)}</td>
            <td><span class="${classeStatus}">${esc(statusTexto)}</span></td>
            <td>${esc(item.dataSolicitacao)}</td>
            <td>
                <div class="dropdown-actions">
                    <button class="btn-acoes" type="button" onclick="toggleActions(this)">Ações ▼</button>
                    <div class="actions-menu hidden">
                        <button type="button" onclick="abrirModalInteresse('status', ${index})">Editar status</button>
                        <button type="button" onclick="abrirModalInteresse('descricao', ${index})">Editar descrição</button>
                        <button type="button" onclick="abrirModalInteresse('tipo', ${index})">Editar tipo</button>
                        <button type="button" onclick="removerInteresse(${index})">Remover</button>
                    </div>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function abrirModalInteresse(campo, index) {
    interesseEditandoIndex = index;
    interesseEditandoCampo = campo;

    const item = interessesAtd[index] || {};
    const statusLower = (item.status || "").toLowerCase();

    const titulos = {
        status: "Editar status",
        tipo: "Editar tipo",
        descricao: "Editar descrição"
    };

    document.getElementById("tituloEditarInteresse").textContent = titulos[campo] || "Editar";

    const area = document.getElementById("areaEditarInteresse");

    if (campo === "status") {
        area.innerHTML = `
            <select id="valorEditarInteresse">
                <option value="Pendente" ${statusLower.includes("pendente") ? "selected" : ""}>Pendente</option>
                <option value="Em andamento" ${statusLower.includes("andamento") ? "selected" : ""}>Em andamento</option>
                <option value="Concluído" ${statusLower.includes("conclu") ? "selected" : ""}>Concluído</option>
            </select>
        `;
    } else if (campo === "tipo") {
        area.innerHTML = `
            <select id="valorEditarInteresse">
                <option value="doar" ${item.tipo === "doar" ? "selected" : ""}>Doar</option>
                <option value="receber" ${item.tipo === "receber" ? "selected" : ""}>Receber</option>
                <option value="ambos" ${item.tipo === "ambos" ? "selected" : ""}>Ambos</option>
            </select>
        `;
    } else {
        area.innerHTML = `
            <input type="text" id="valorEditarInteresse" value="${esc(item.necessidade)}" placeholder="Nova descrição">
        `;
    }

    document.getElementById("modalEditarInteresse").classList.remove("hidden");
}

async function salvarEdicaoInteresse() {
    const valorEl = document.getElementById("valorEditarInteresse");
    const valor = valorEl ? valorEl.value.trim() : "";

    if (!valor) {
        alert("Preencha o campo.");
        return;
    }

    const item = interessesAtd[interesseEditandoIndex];
    if (!item) return;

    if (interesseEditandoCampo === "descricao") {
        const erroDescricao = Validacoes.descricao(valor);
        if (erroDescricao) {
            alert(erroDescricao);
            return;
        }
    }

    const corpo = {};
    corpo[interesseEditandoCampo] = valor;

    const r = await chamarApi("PUT", "/interesses/" + item.id, corpo);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível salvar."));
        return;
    }

    fecharModalInteresse();
    carregarInteressesAtendente();
    abrirSucessoAtendente();
}

async function removerInteresse(index) {
    const item = interessesAtd[index];
    if (!item) return;

    if (!confirm("Remover este interesse?")) return;

    const r = await chamarApi("DELETE", "/interesses/" + item.id);
    if (!r.ok) {
        alert(mensagemErro(r, "Não foi possível remover."));
        return;
    }

    carregarInteressesAtendente();
}

/* ============================================================
   DADOS DA ONG (só leitura, vêm do banco)
============================================================ */

async function carregarDadosOngAtendente() {
    const r = await chamarApi("GET", "/ong");
    if (!r.ok || !r.dados) return;

    const spans = document.querySelectorAll("#dadosONGAtendente .info span");
    /* ordem na tela: Nome, Telefone, Email, Endereço, CNPJ */
    const valores = [
        r.dados.nome,
        formatarTelefone(r.dados.telefone),
        r.dados.email,
        r.dados.endereco,
        r.dados.cnpj
    ];

    spans.forEach((span, i) => {
        if (valores[i] !== undefined && valores[i] !== "") span.textContent = valores[i];
    });
}

document.addEventListener("DOMContentLoaded", carregarDadosOngAtendente);
