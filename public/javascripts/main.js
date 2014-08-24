$(function(){
	var userId =$('#dataDiv').data('userId');

	var displayAlert = (function(){
		var alertTemplate = $('#alertTmpl').html(),
		alertDiv = $('#alertDiv');
		return function(msg){
			alertDiv.html(Mustache.render(alertTemplate, {msg: msg}));
			alertDiv.find('div').addClass('alert alert-success')
		}
	})();
	var displayFieldError = function(propertyName, errors) {
		if(errors[propertyName]) {
			var errorHtml = Mustache.render($('#errorTmpl').html(), {msg: errors[propertyName][0]});
			$('#' + propertyName).after(errorHtml);
		}
	};

	// Contacts section

	var contactModal = $('#contactModal');
	$('#addContactBtn').click(function(){
		contactModal.modal('show');
	});
	contactModal.find('#closeBtn').click(function(){
		contactModal.modal('hide');
	});
	contactModal.on('hidden', function(){
		clearContactForm();
		registerSaveCallback(createContactCallback);
	});
	var clearFormErrors = function(){
		contactModal.find('.error').remove();
	};
	var clearContactForm = function(){
		$('#contactType').val('');
		$('#contact').val('');
		clearFormErrors();
	};
	var contactView = (function(){
		var contactTemplate = $('#contactTmpl').html();
		return function(contact) {
			var route = jsRoutes.controllers.Contacts['delete'](userId, contact.id)
			var data = {
				contact: contact,
				form: {
					action: route.url,
					method: route.method
				}
			};
			return Mustache.render(contactTemplate, data);
		}
	})();
	var addContactView = (function(){
		var contacts = $('#contacts');
		return function(contact){
			contacts.append(contactView(contact));
		}
	})();
	var commonSaveCallback = function(route, success){
		clearFormErrors();
		var contact = {
				contactType: $('#contactType').val(),
				contact: $('#contact').val()
			};
		route.ajax({
			data: contact,
			dataType: 'json'
		})
		.success(function(data){
			success(data, contact);
			contactModal.modal('hide');
			clearContactForm();
		})
		.error(function(data){
				var errors = $.parseJSON(data.responseText).errors;
				displayFieldError('contactType', errors);
				displayFieldError('contact', errors);
			});
	};
	var createContactCallback = function(){
		commonSaveCallback(
			jsRoutes.controllers.Contacts.save(userId),
			function(data, contact){
				contact.id = data.properties.id;
				displayAlert('Contact saved!');
				addContactView(contact);
				registerEditCallback();
			});
	};
	var registerSaveCallback = function(callback){
		contactModal.find('#saveBtn').unbind('click');
		contactModal.find('#saveBtn').click(callback);
	};
	registerSaveCallback(createContactCallback);
	var editCallback = function(){
		var parentLi = $(this).parent();
		$('#contactType').val(parentLi.data('contactType'));
		$('#contact').val(parentLi.data('contact'));
		contactModal.modal('show');
		registerSaveCallback(function(){
			updateContactCallback(parentLi.data('contactId'), parentLi);
		});
	};
	var registerEditCallback = function(){
		$('.editContact').unbind('click');
		$('.editContact').click(editCallback);
	};
	registerEditCallback();
	var updateContactCallback = function(contactId, contactLi) {
		commonSaveCallback(
			jsRoutes.controllers.Contacts.update(userId, contactId),
			function(data, contact){
				contact.id = contactId;
				displayAlert('Contact updated!');
				contactLi.replaceWith(contactView(contact));
				registerSaveCallback(createContactCallback);
				registerEditCallback();
			});
	};

	// User section

	var userModal = $('#userModal');
	$('#editUserBtn').click(function(){
		$('#username').val($('#username-value').text().trim());
		$('#age').val($('#age-value').text().trim());
		userModal.modal('show');
	});
	userModal.find('#closeBtn').click(function(){
		userModal.modal('hide');
	});
	userModal.on('hidden', function(){
		clearUserFormErrors();
		registerSaveCallback(createContactCallback);
	});
	var clearUserFormErrors = function(){
		userModal.find('.error').remove();
	};
	userModal.find('#saveBtn').click(function(){
		var username = $('#username').val(),
			age = $('#age').val()
		jsRoutes.controllers.Users.update(userId)
			.ajax({
				data: {
					username: username,
					age: age
				},
				dataType: 'json'
			})
			.success(function(){
				$('#username-value').text(username);
				$('#age-value').text(age);
				displayAlert('User updated!');
				userModal.modal('hide');
			})
			.error(function(data){
				var errors = $.parseJSON(data.responseText).errors;
				displayFieldError('username', errors);
				displayFieldError('age', errors);
			});
	});
});
