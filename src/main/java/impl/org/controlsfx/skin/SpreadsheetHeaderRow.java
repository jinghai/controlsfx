/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.skin;

import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.SpreadsheetView.SpreadsheetViewSelectionModel;
import org.controlsfx.control.spreadsheet.model.SpreadsheetCell;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;

public class SpreadsheetHeaderRow extends TableHeaderRow {

    SpreadsheetViewSkin spreadsheetViewSkin;
    public SpreadsheetHeaderRow(final SpreadsheetViewSkin skin) {
        super(skin);
        spreadsheetViewSkin = skin;
    }
    public void init() {
        final Runnable r = new Runnable() {
            @Override
            public void run() {

                spreadsheetViewSkin.spreadsheetView.showRowHeaderProperty().addListener(
                        rowHeaderListener);
                selectionModel = spreadsheetViewSkin.spreadsheetView
                        .getSelectionModel();
                selectionModel.getSelectedColumns().addListener(
                        selectionListener);
                spreadsheetViewSkin.spreadsheetView.getFixedColumns()
                        .addListener(fixedColumnsListener);

                spreadsheetViewSkin.getTableMenuButtonVisibleProperty()
                        .addListener(new InvalidationListener() {
                            @Override
                            public void invalidated(Observable valueModel) {
                                if (working) {
                                    requestLayout();
                                }
                            }
                        });

                /*****************************************************************
                 * MODIFIED BY NELLARMONIA
                 *****************************************************************/
                // We listen to the BooleanProperty linked with the CheckBox of
                // the columnHeader
                spreadsheetViewSkin.spreadsheetView.showColumnHeaderProperty()
                        .addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(
                                    ObservableValue<? extends Boolean> arg0,
                                    Boolean arg1, Boolean arg2) {
                                working = arg2;
                                requestLayout();
                                getRootHeader().layoutFixedColumns();
                                updateHighlighSelection();
                            }
                        });
                /*****************************************************************
                 * END OF MODIFIED BY NELLARMONIA
                 *****************************************************************/
            }
        };
        Platform.runLater(r);
    }

    protected void updateTableWidth() {
        super.updateTableWidth();
        // snapping added for RT-19428
        double padding = 0;
        /*****************************************************************
         * MODIFIED BY NELLARMONIA
         *****************************************************************/
        if (spreadsheetViewSkin != null
                && spreadsheetViewSkin.spreadsheetView != null
                && spreadsheetViewSkin.spreadsheetView.showRowHeaderProperty().get()) {
            padding += spreadsheetViewSkin.getRowHeaderWidth();
        }

        /*****************************************************************
         * END OF MODIFIED BY NELLARMONIA
         *****************************************************************/
        Rectangle clip = ((Rectangle) getClip());
        clip.setWidth(clip.getWidth() == 0 ? 0 : clip.getWidth() - padding);
    }

    protected void updateScrollX() {
        super.updateScrollX();
        /*****************************************************************
         * MODIFIED BY NELLARMONIA
         *****************************************************************/
        if (working) {
            requestLayout();
            getRootHeader().layoutFixedColumns();
        }
        /*****************************************************************
         * END OF MODIFIED BY NELLARMONIA
         *****************************************************************/
    }

    // Indicate wether the this TableHeaderRow is activated or not
    private Boolean working = true;

    /**
     * When the Rowheader is showing (or not anymore) we need to react
     * accordingly
     */
    private final ChangeListener<Boolean> rowHeaderListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
			 updateTableWidth();
		}
    };
    
    /**
     * When we fix/unfix some columns, we change the style of the Label header text
     */
    private final ListChangeListener<Integer> fixedColumnsListener = new ListChangeListener<Integer>() {

		@Override
		public void onChanged(
				javafx.collections.ListChangeListener.Change<? extends Integer> arg0) {
			while(arg0.next()){
				//If we unfix a column
				for (Integer remitem : arg0.getRemoved()) {
                   removeStyleHeader(remitem);
                }
				//If we fix one
                for (Integer additem : arg0.getAddedSubList()) {
                	addStyleHeader(additem);
                }
			}
			 updateHighlighSelection();
		}
	}; 

	/**
	 * Add the fix style of the header Label of the specified column
	 * @param i
	 */
	private void removeStyleHeader(Integer i) {
        	getRootHeader().getColumnHeaders().get(i).getChildrenUnmodifiable().get(0).getStyleClass().removeAll("fixed");
    }
	/**
	 * Remove the fix style of the header Label of the specified column
	 * @param i
	 */
	private void addStyleHeader(Integer i) {
            getRootHeader().getColumnHeaders().get((Integer) i)
                    .getChildrenUnmodifiable().get(0).getStyleClass()
                    .addAll("fixed");
    }
    private SpreadsheetViewSelectionModel<ObservableList<SpreadsheetCell>> selectionModel;
    
    
    /**
     * When we select some cells, we want the header to be highlighted
     */
    private final InvalidationListener selectionListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable valueModel) {
            updateHighlighSelection();
        }
    };
    
    /**
     * Highlight the header Label when selection change.
     */
    private void updateHighlighSelection() {
    	for (final TableColumnHeader i : getRootHeader().getColumnHeaders()) {
            i.getChildrenUnmodifiable().get(0).getStyleClass().removeAll("selected");

        }
        final List<Integer> selectedColumns = selectionModel
                .getSelectedColumns();
        // TODO Ugly hack to get access to the Label
        for (final Object i : selectedColumns) {
            getRootHeader().getColumnHeaders().get((Integer) i)
                    .getChildrenUnmodifiable().get(0).getStyleClass()
                    .addAll("selected");
        }

    }

    protected NestedTableColumnHeader createRootHeader() {
        return new SpreadsheetNestedTableColumnHeader(getTableSkin(), null);
    }

    public SpreadsheetNestedTableColumnHeader getRootHeader() {
        return (SpreadsheetNestedTableColumnHeader) super.getRootHeader();
    }

}
