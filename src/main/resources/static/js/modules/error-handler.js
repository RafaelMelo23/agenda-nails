
const ErrorHandler = {
    lastMessage: null,
    lastTime: 0,

    translations: {
        'Insufficient permissions.': 'Você não tem permissão para realizar esta ação.',
        'Access Denied': 'Acesso negado.',
        'Bad credentials': 'E-mail ou senha incorretos.',
        'User is disabled': 'Usuário desativado.',
        'Token expired': 'Sessão expirada. Por favor, faça login novamente.',
        'Internal Server Error': 'Ocorreu um erro interno no servidor. Tente novamente mais tarde.',
        'Forbidden': 'Você não tem permissão para acessar este recurso.',
        'Unauthorized': 'Você precisa estar logado para acessar esta página.',
        'User already exists': 'Este e-mail já está cadastrado.',
        'Professional not found': 'Profissional não encontrado.',
        'Schedule conflict': 'Este horário não está mais disponível. Por favor, escolha outro.',
        'Full authentication is required to access this resource': 'Sua sessão expirou. Por favor, entre novamente.'
    },

    statusDefaults: {
        401: 'Sua sessão expirou ou você não está autenticado.',
        403: 'Você não tem permissão para acessar esta área.',
        404: 'O recurso solicitado não foi encontrado.',
        409: 'Houve um conflito ao processar sua solicitação.',
        422: 'Os dados informados são inválidos.',
        500: 'Ocorreu um erro interno no servidor.'
    },

    parseError: async function(response) {
        try {
            const data = await response.json();
            const rawMessages = data.message || data.messages || [];
            
            if (rawMessages.length === 0 && data.error) {
                rawMessages.push(data.error);
            }

            if (rawMessages.length === 0) {
                return [this.statusDefaults[response.status] || 'Ocorreu um erro inesperado.'];
            }

            return rawMessages.map(msg => {
                const message = msg.includes(': ') ? msg.split(': ')[1] : msg;
                return this.translations[message] || message;
            });
        } catch (e) {
            return [this.statusDefaults[response.status] || 'Erro ao processar resposta do servidor.'];
        }
    },

    handle: async function(response) {
        const messages = await this.parseError(response);
        const fullMessage = messages.join('\n');
        
        // Simple deduplication: avoid showing the same message multiple times in 500ms
        const now = Date.now();
        if (this.lastMessage === fullMessage && (now - this.lastTime) < 500) {
            return fullMessage;
        }
        
        this.lastMessage = fullMessage;
        this.lastTime = now;

        Toast.error(fullMessage);

        return fullMessage;
    }
};
