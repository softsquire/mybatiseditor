package org.eclipselabs.mybatiseditor.ui.view;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipselabs.mybatiseditor.ui.reader.MyBatisDomReader;
import org.eclipselabs.mybatiseditor.ui.reader.MyBatisSqlParser;
import org.w3c.dom.Element;

@SuppressWarnings("restriction")
public class MyBatisSqlView extends ViewPart {
	
	private static final String BACKGROUND_PREF_ID = "org.eclipselabs.mybatiseditor.ui.mybatissqlviewBackground";
	
	private static final String TEXTCOLOR_PREF_ID = "org.eclipselabs.mybatiseditor.ui.mybatissqlviewTextColor";
	
	private static final String FONT_PREF_ID = "org.eclipselabs.mybatiseditor.ui.mybatissqlviewFont";

    private final class MyBatisSqlViewSelectionListener implements ISelectionListener {

        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            if (!selection.isEmpty() && (selection instanceof IStructuredSelection)) {
                IStructuredSelection sel = (IStructuredSelection) selection;
                if (sel.size() == 1) {
                    Object firstElement = sel.getFirstElement();
                    MyBatisDomReader reader = new MyBatisDomReader();
                    if ((firstElement instanceof ElementImpl) && (selection instanceof ITextSelection)) {
                        ElementImpl selectedElement = (ElementImpl) firstElement;
                        ITextSelection textSel = (ITextSelection) selection;
                        firstElement =
                                reader.getCurrentMyBatisNode(selectedElement.getStructuredDocument(),
                                        textSel.getOffset());
                    }
                    if (firstElement instanceof AttrImpl) {
                        AttrImpl attr = (AttrImpl) firstElement;
                        Element ownerElement = attr.getOwnerElement();
                        if (ownerElement instanceof ElementImpl) {
                            ElementImpl element = (ElementImpl) ownerElement;
                            String newText =
                                    new MyBatisSqlParser().getSqlText(element.getStructuredDocument(), element.getLocalName(),
                                            attr.getNodeValue());
                            if ((newText != null) && !newText.equals(text.getText())) {
                                text.setText(newText);
                            }
                        }
                    }
                }
            }
        }
    }

    private final class MyBatisSqlViewPropertyChangeListener implements IPropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (FONT_PREF_ID.equals(event.getProperty()) 
            		|| BACKGROUND_PREF_ID.equals(event.getProperty())
            		|| TEXTCOLOR_PREF_ID.equals(event.getProperty())) {
            	final Display display= getSite().getPage().getWorkbenchWindow().getWorkbench().getDisplay();
				if (!display.isDisposed()) {
					display.asyncExec(new Runnable() {
						public void run() {
							if (!display.isDisposed()) {
								setAppearance();
							}
						}
					});
				}
            }            
        }
    }

    private MyBatisSqlViewPropertyChangeListener themeListener;

    private MyBatisSqlViewSelectionListener selectionListener;

    protected Text text;

    @Override
    public void createPartControl(Composite parent) {
        selectionListener = new MyBatisSqlViewSelectionListener();
        getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(selectionListener);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new FillLayout());
        text = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
        setAppearance();

        themeListener = new MyBatisSqlViewPropertyChangeListener();
        PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(themeListener);
    }

    protected void setAppearance() {
    	ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
    	text.setFont(currentTheme.getFontRegistry().get(FONT_PREF_ID));
    	text.setBackground(currentTheme.getColorRegistry().get(BACKGROUND_PREF_ID));
    	text.setForeground(currentTheme.getColorRegistry().get(TEXTCOLOR_PREF_ID));
    }

    @Override
    public void dispose() {
        if (selectionListener != null) {
            getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(selectionListener);
            selectionListener = null;
        }
        if (themeListener != null) {
            PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(themeListener);
            themeListener = null;
        }
        super.dispose();
    }

    @Override
    public void setFocus() {
    }
}
