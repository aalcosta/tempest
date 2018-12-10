(function($) {

	/**
	 * Cache do bundle I18N
	 */
	var _pageBundle = null;

	/**
	 * Cache das de templates doT compilados
	 */
	var _pageCache = [];

	/**
	 * Fila de mensagens Estrutura JSON : {target: , content: , type: }
	 */
	var _queuedMessage : null,


	tempest = {

		/**
		 * Configuracoes de convencoes
		 */	
		config: {
			appHome : 'home',				// URL utilizada no HASH do permalink
			appServices : '/s/',			// Base das URLs de mapeamento das Servlets/Acoes
			appTemplates : '/templates/',   // Base das URLs de templates
			appBundle : '/bundle',			// URL para carga do mapa/bundle de i18N
		},

		/**
		 * Faz a navegacao para uma pagina de destino utilizando o HASH da URL
		 * 
		 * @param urlHash
		 *            Valor da URL a ser invocada apos a navegacao
		 * @param params
		 *            Objeto JSON representandos os valores de parametros a serem adicionados no GET
		 */
		navigate : function(urlHash, params) {
			var paramUrl;
			if (params) {
				paramUrl = $.param(params);
			}
			if (paramUrl) {
				urlHash += '?' + paramUrl;
			}
			var actualHash = (window.location.hash + '').substring(1);
			if (urlHash === actualHash) {
				_hashChanged();
			} else {
				window.location.hash = url;
			}
		},

		/**
		 * Invocacao AJAX de um servico JSON
		 * 
		 * @param reqMethod
		 *            Request method utilizado
		 * @param reqURL
		 *            URL para ser invocada
		 * @param param
		 *            Objeto JSON com os parametros da invocacao
		 */
		invoke : function(reqMethod, reqUrl, params) {
			var deferred = new $.Deferred();
			var xhr = $.ajax({
				type : reqMethod,
				url : tempest.appServices + reqUrl,
				data : params,
				datatype : "json"
			}).done(function(data) {
				$.cookie('token', xhr.getResponseHeader('token'));
				deferred.resolve(data);
			}).fail(function(xhr) {
				deferred.reject(xhr.status);
			});
			return deferred.promise();
		},

		/**
		 * Rendezia um template HTML/doT num determinado elemento do DOM. <br>
		 * NOTA: Para melhoria de performance a carga do template e feita apenas uma vez
		 * 
		 * @param target:
		 *            Seletor CSS do elemento do DOM onde o template sera renderizado
		 * @param templatePath
		 *            caminho do arquivo HTML de template
		 * @param data
		 *            (opcional): Objeto JSON que sera utilizado como contexto {it} do doT
		 */
		render : function(target, templatePath, data) {
			target = $(target);
			templatePath = tempest.appTemplates + templatePath;
			if (!data) { data = {}; }

			var deferred = new $.Deferred();

			_loadBundle().done(function() {
				data.bundle = tempest.pageBundle;
				_loadTemplate(templatePath).done(function(template) {
					target.html(template(data));
					tempest.showMessage();
					deferred.resolve(target);
				});
			});
			return deferred.promise();
		},


		/**
		 * Metodo utilitario para execucao de um upload de dados para o servidor
		 * 
		 * @param formUpload
		 *            Seletor CSS contendo os dados a serem enviados ao servidor
		 * @param uploadURL
		 *            URL de destino do upload
		 * @param iframe:
		 *            Seletor CSS do IFRAME que utilizadopara o upload.<br>
		 *            NOTA: O IFRAME pode estar invisivel.
		 */
		upload : function(formUpload, uploadUrl, iframe) {
			var deferred = new $.Deferred();

			$(document).unbind('trigger');
			$(document).bind('trigger', function(ret) {
				tempest.queueMessage('Upload do documento concluído com sucesso!', 'success');
				deferred.resolve(ret);
			});

			formUpload.attr('action', uploadUrl);
			formUpload.attr('target', iframe.attr('id'));
			formUpload.attr('method', 'post');
			formUpload.attr('enctype', 'multipart/form-data');
			formUpload.attr('encoding', 'multipart/form-data');
			formUpload.submit();

			return deferred.promise();
		},

		/**
		 * Registra/enfilera uma mensagem para ser exibida na proxima renderizacao de pagina, onde seja encontrado o elemento TARGET
		 * 
		 * @param target:
		 *            Seletor CSS indicando o container onde a mensagem sera exibida
		 * @param content
		 *            Conteudo HTML a ser escrito na mensagem
		 * @param type
		 *            (opcional) Tipo de mensagem a ser renderizado, variavel para a renderizacao do template da mensagem
		 */
		queueMessage : function(target, content, type) {
			if (!type) { type = 'info'; }

			tempest.queuedMessage = {
				target : target,
				content : content,
				type : type
			};
		},

		/**
		 * Metodo utilizado para mostrar e descarregar a mensagem armazendada na fila de mensagens.
		 * 
		 * @param target
		 *            Seletor CSS indicando o container onde a mensagem sera exibida
		 * @param content
		 *            Conteudo HTML a ser escrito na mensagem
		 * @param type
		 *            Tipo de mensagem a ser renderizado, variavel para a renderizacao do template da mensagem
		 */
		showMessage : function(target, content, type) {
			if (content) {
				tempest.queueMessage(target, content, type);
			}

			if (tempest.queuedMessage) {
				tempest.loadTemplate(this.appTemplates + 'mensagem.html').done(function(template) {
					$(tempest.queuedMessage.target).html(template(tempest.queuedMessage));
					tempest.queuedMessage = null;
				});
			}
		}

	}


	/**
	 * Busca e, se necessario carga, do bundle de internacionalizacao.<br>
	 * A chave do bundle no cache eh o idioma/locale retornado pelo servidor.
	 */
	function _loadBundle() {
		var deferred = new $.Deferred();
		if (tempest.pageBundle) {
			deferred.resolve();
		} else {
			$.ajax({
				type : 'GET',
				url : tempest.appBundle,
				datatype : 'json'
			}).fail(function() {
				tempest.pageBundle = {};
				deferred.resolve();
			}).done(function(ret) {
				tempest.pageBundle[ret.locale] = ret.bundle;
				deferred.resolve();
			});
		}
		return deferred.promise();
	}

	/**
	 * Busca e, se necessario carga, dos templates e calculo da funcao <b>doT</b> no cache de templates.
	 * 
	 * @param templateURL
	 *            caminho do template. Utilizado como chave do cache
	 */
	function _loadTemplate(templateUrl) {
		var deferred = new $.Deferred();
		if (tempest.pageCache[templateUrl]) {
			deferred.resolve(tempest.pageCache[templateUrl]);
		} else {
			$.ajax({
				type : 'GET',
				url : templateUrl,
				datatype : 'html'
			}).done(function(htmlTemplate) {
				tempest.pageCache[templateUrl] = doT.template(htmlTemplate);
				deferred.resolve(tempest.pageCache[templateUrl]);
			});
		}
		return deferred.promise();
	}


	// Controle de links e hashes
	window.onhashchange = function() {
		_hashChanged();
	}

	window.onload = function() {
		var hash = (window.location.hash + '');
		if (!hash) {
			window.location.hash = tempest.appHome;
		} else {
			_hashChanged();
		}
	}

	function _hashChanged() {
		var hash = (window.location.hash + '').substring(1).split('?');
		if (!hash) {
			window.location.hash = tempest.appHome;
			return;
		}

		var path = hash[0].split('/');

		var control = window[path[0]];
		var action = path[1];

		var params = null;
		if (hash[1]) {
			params = JSON.parse('{"' + decodeURI(hash[1]).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g, '":"') + '"}');
		}

		control[action](params);
	}

})(jQuery);