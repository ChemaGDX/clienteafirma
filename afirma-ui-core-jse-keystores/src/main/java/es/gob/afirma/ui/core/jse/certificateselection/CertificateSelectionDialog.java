/*
 * Este fichero forma parte del Cliente @firma.
 * El Cliente @firma es un aplicativo de libre distribucion cuyo codigo fuente puede ser consultado
 * y descargado desde www.ctt.map.es.
 * Copyright 2009,2010,2011 Gobierno de Espana
 * Este fichero se distribuye bajo  bajo licencia GPL version 2  segun las
 * condiciones que figuran en el fichero 'licence' que se acompana. Si se distribuyera este
 * fichero individualmente, deben incluirse aqui las condiciones expresadas alli.
 */

package es.gob.afirma.ui.core.jse.certificateselection;

import java.awt.Color;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import es.gob.afirma.core.keystores.KeyStoreManager;
import es.gob.afirma.core.keystores.NameCertificateBean;
import es.gob.afirma.keystores.KeyStoreUtilities;

/** Di&aacute;logo de selecci&oacute;n de certificados con est&eacute;tica similar al de Windows 7.
 * @author Carlos Gamuci
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class CertificateSelectionDialog extends MouseAdapter {

	private final CertificateSelectionPanel csd;

	private final JOptionPane optionPane;
	
	private final Component parent;
	
	private KeyStoreManager ksm;


	/** Construye el di&aacute;logo de selecci&oacute;n de certificados a partir del listado con
	 * sus nombres y los propios certificados.
	 * @param el Listado de certificados.
	 * @param parent Componente sobre el que se mostrar&aacute; el di&aacute;logo. */
	public CertificateSelectionDialog(final Component parent, final NameCertificateBean[] el, final KeyStoreManager ksm) {

	    if (el == null || el.length == 0) {
	        throw new IllegalArgumentException("El listado de certificados no puede ser nulo ni vacio"); //$NON-NLS-1$
	    }

	    this.ksm = ksm;
	    
		this.csd = new CertificateSelectionPanel(el.clone());
		this.parent = parent;
		this.optionPane = el.length > 1 ?
				new CertOptionPane(this.csd) : new JOptionPane();

		this.csd.addCertificateListMouseListener(this);

		this.optionPane.setMessage(this.csd);
		this.optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
		this.optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);

	}

	/** Muestra el di&aacute;logo de selecci&oacute;n de certificados.
	 * @return Nombre del certificado seleccionado o {@code null} si el usuario
	 * lo cancela o cierra sin seleccionar. */
	public String showDialog() {

		final JDialog certDialog = this.optionPane.createDialog(
			this.parent,
			CertificateSelectionDialogMessages.getString("CertificateSelectionDialog.0") //$NON-NLS-1$
		);

		certDialog.setBackground(Color.WHITE);
		certDialog.setModal(true);

		final KeyEventDispatcher dispatcher = new CertificateSelectionDispatcherListener(
				this.optionPane,
				this.ksm,
				this
				); 
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);

		certDialog.setVisible(true);

		if (this.optionPane.getValue() == null ||
				((Integer) this.optionPane.getValue()).intValue() != JOptionPane.OK_OPTION) {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
			certDialog.dispose();
			return null;
		}
		final String selectedAlias = this.csd.getSelectedCertificate();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
		certDialog.dispose();
		return selectedAlias;
	}

	/** {@inheritDoc} */
	@Override
	public void mouseClicked(final MouseEvent me) {
		if (me.getClickCount() == 2 && this.optionPane != null) {
			this.optionPane.setValue(Integer.valueOf(JOptionPane.OK_OPTION));
		}
	}

	private static final class CertOptionPane extends JOptionPane {

		private static final long serialVersionUID = 1L;

		private final CertificateSelectionPanel selectionPanel;

		CertOptionPane(final CertificateSelectionPanel csd) {
			this.selectionPanel = csd;
		}

		/** {@inheritDoc} */
		@Override
        public void selectInitialValue() {
			this.selectionPanel.selectCertificateList();
        }
	}

	/** Refresca el di&aacute;logo con los certificados del mismo almac&eacute;n. */
	public void refresh() {

		try {
			final Map<String, String> aliases = KeyStoreUtilities.getAliasesByFriendlyName(
					this.ksm.getAliases(),
					this.ksm,
					false,
					true,
					null);

			this.csd.refresh(KeyStoreUtilities.getNameCertificateBeans(
					aliases.keySet().toArray(new String[aliases.size()]),
					this.ksm)
					);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void changeKeyStore(final KeyStoreManager ksm) {
		
		this.ksm = ksm;
		try {
			final Map<String, String> aliases = KeyStoreUtilities.getAliasesByFriendlyName(
					this.ksm.getAliases(),
					this.ksm,
					false,
					true,
					null);

			this.csd.refresh(KeyStoreUtilities.getNameCertificateBeans(
					aliases.keySet().toArray(new String[aliases.size()]),
					this.ksm)
					);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
