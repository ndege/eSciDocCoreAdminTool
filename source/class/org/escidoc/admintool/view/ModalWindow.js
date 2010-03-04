/*
 * CDDL HEADER START
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License, Version 1.0 only (the "License"). You may not use
 * this file except in compliance with the License.
 * 
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or
 * http://www.escidoc.de/license. See the License for the specific language
 * governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at license/ESCIDOC.LICENSE. If applicable, add the
 * following below this CDDL HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 * 
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2010 Fachinformationszentrum Karlsruhe Gesellschaft fuer
 * wissenschaftlich-technische Information mbH and Max-Planck- Gesellschaft zur
 * Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to
 * license terms.
 */
/**
 * @author CHH
 */
qx.Class.define("org.escidoc.admintool.view.ModalWindow", {
			extend : qx.ui.window.Window,
			construct : function() {
				this.base(arguments);
				this._initSelf();
				this._addUserForm();
			},
			properties : {},
			members : {
				__userAccountForm : null,
				_initSelf : function() {
					this.setLayout(new qx.ui.layout.VBox(10));
					this.setModal(true);
					this.setShowMaximize(false);
					this.setShowMinimize(false);
					this.setShowStatusbar(false);
                    this.setMovable(false);
                    
					// TODO: set not movable.
					// refactor this, calculate the middle point from browser
					// window size.
					this.moveTo(350, 100);
				},
				_addUserForm : function() {
					var test = new org.escidoc.admintool.view.Form();
					this.add(test);
					// this.__userAccountForm = new
					// org.escidoc.admintool.view.Form();
					// this.add(this.__userAccountForm);
				}
			},
			destruct : function() {
				this._disposeObjects();
			}
		});