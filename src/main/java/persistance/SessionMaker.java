package persistance;

import org.json.JSONArray;
import org.json.JSONObject;

import controller.ConsoleController;
import mediator.ControllerMediator;
import java.util.ArrayList;

public class SessionMaker {
    public static final String GTF_PATH_KEY = "gtf";
    public static final String MATRIX_PATH_KEY = "matrix";
    public static final String ISOFORM_LABELS_PATH_KEY = "isoform_ids";
    public static final String CELL_LABELS_PATH_KEY = "cell_labels";
    public static final String EMBEDDING_PATH_KEY = "embedding";
    public static final String CELL_PLOT_CLEARED_KEY = "cell_plot_cleared";
    public static final String CELLS_SELECTED_KEY = "cells_selected";
    public static final String CELL_CATEGORIES_SELECTED_KEY = "cell_categories_selected";
    public static final String LABEL_SET_IN_USE_KEY = "label_set_in_use";
    public static final String GENES_SHOWN_KEY = "genes_shown";
    public static final String ISOFORMS_SELECTED_KEY = "isoforms_selected";
    public static final String ISOFORM_PLOT_OPEN_KEY = "isoform_open";
    public static final String CLUSTER_VIEW_OPEN_KEY = "cluster_view_open";
    public static final String CONSOLE_OPEN_KEY = "console_open";
    public static final String REVERSE_COMPLEMENT_KEY = "rev_complement";
    public static final String HIDE_SINGLE_EXON_ISOFORMS_KEY = "hide_single_exon_isoforms";
    public static final String HIDE_DOT_PLOT_KEY = "hide_dot_plot";
    public static final String SHOW_ISOFORM_PLOT_LEGEND_KEY = "show_isoform_plot_legend_key";
    public static final String SHOW_MEDIAN_KEY = "show_median";
    public static final String SHOW_AVERAGE_KEY = "show_average";
    public static final String INCLUDE_ZEROS_KEY = "include_zeros";
    public static final String SHOW_GENE_NAME_AND_ID_KEY = "show_gene_name_and_id";
    public static final String SHOW_GENE_NAME_KEY = "show_gene_name";
    public static final String SHOW_GENE_ID_KEY = "show_gene_id";
    public static final String SHOW_ISOFORM_NAME_KEY = "show_isoform_name";
    public static final String SHOW_ISOFORM_ID_KEY = "show_isoform_id";
    public static final String COLOR_CELL_PLOT_BY_ISOFORM_KEY = "color_cell_plot_by_isoform";
    public static final String MIN_TPM_KEY = "min_tpm_key";
    public static final String MAX_TPM_KEY = "max_tpm_key";
    public static final String MIN_TPM_COLOR_KEY = "min_tpm_color_key";
    public static final String MID_TPM_COLOR_KEY = "mid_tpm_color_key";
    public static final String MAX_TPM_COLOR_KEY = "max_tpm_color_key";
    public static final String TPM_SCALE_KEY = "tpm_scale_key";
    public static final String OPTION_FILTERING_BY_KEY = "option_filtering_by";
    public static final String DIS_MIN_TPM_KEY = "dis_min_tpm_key";
    public static final String DIS_MIN_PERCENT_EXPRESSED_KEY = "dis_min_percent_expressed_key";
    public static final String DE_SELECTED_CATEGORIES_KEY = "de_selected_categories_key";
    public static final String DE_MIN_FOLD_CHANGE_KEY = "de_min_fold_change_key";
    public static final String DE_MIN_TPM_KEY = "de_min_tpm_key";
    public static final String DE_MIN_PERCENT_EXPRESSED_KEY = "de_min_percent_expressed_key";
    public static final String CSE_SELECTED_CATEGORIES_KEY = "cse_selected_categories_key";
    public static final String CSE_MIN_TPM_KEY = "cse_min_tpm_key";
    public static final String CSE_MIN_PERCENT_EXPRESSED_KEY = "cse_min_percent_expressed_key";
    public static final String CSE_MAX_TPM_KEY = "cse_max_tpm_key";
    public static final String CSE_MAX_PERCENT_EXPRESSED_KEY = "cse_max_percent_expressed_key";
    public static final String USING_UMAP_FOR_EMBEDDING_KEY = "using_umap_for_embedding_key";
    public static final String PERPLEXITY_KEY = "perplexity_key";
    public static final String MAX_ITERATIONS_KEY = "max_iterations_key";
    public static final String MIN_DIST_KEY = "min_dist_key";
    public static final String NEAREST_NEIGHBORS_KEY = "nearest_neighbors_key";
    public static final String FIGURE_SCALE_KEY = "figure_scale";
    public static final String FIGURE_TYPE_EXPORTING_KEY = "figure_type_exporting";

    /**
     * Creates a session containing:
     *   - the current loaded path
     *   - whether the isoform plot, cluster view and console are opened or closed
     *   - the current isoform plot settings
     *   - the console messages
     */
    public static JSONObject makeSession(String pathToDir) {
        ControllerMediator.getInstance().exportEmbeddingToFile(pathToDir);
        ControllerMediator.getInstance().exportLabelSetsToFiles(pathToDir);
        JSONObject session = new JSONObject();
        session.put(GTF_PATH_KEY, CurrentSession.getGTFPath());
        session.put(MATRIX_PATH_KEY, CurrentSession.getMatrixPath());
        session.put(ISOFORM_LABELS_PATH_KEY, CurrentSession.getIsoformIDsPath());
        session.put(CELL_LABELS_PATH_KEY, CurrentSession.getLabelSetPaths());
        session.put(EMBEDDING_PATH_KEY, CurrentSession.getEmbeddingPath());
        session.put(CELL_PLOT_CLEARED_KEY, ControllerMediator.getInstance().isCellPlotCleared());
        session.put(CELLS_SELECTED_KEY, ControllerMediator.getInstance().getSelectedCellNumbers());
        session.put(CELL_CATEGORIES_SELECTED_KEY, ControllerMediator.getInstance().getSelectedCellCategoryNames());
        session.put(CELLS_SELECTED_KEY, ControllerMediator.getInstance().getSelectedCellNumbers());
        session.put(CELL_CATEGORIES_SELECTED_KEY, ControllerMediator.getInstance().getSelectedCellCategoryNames());
        session.put(LABEL_SET_IN_USE_KEY, ControllerMediator.getInstance().getLabelSetInUse().getName());
        session.put(GENES_SHOWN_KEY, ControllerMediator.getInstance().getShownGeneIDs());
        session.put(ISOFORMS_SELECTED_KEY, ControllerMediator.getInstance().getSelectedIsoformIDs());
        session.put(ISOFORM_PLOT_OPEN_KEY, ControllerMediator.getInstance().isIsoformPlotOpen());
        session.put(CLUSTER_VIEW_OPEN_KEY, ControllerMediator.getInstance().isClusterViewOpen());
        session.put(CONSOLE_OPEN_KEY, ControllerMediator.getInstance().isConsoleOpen());
        session.put(REVERSE_COMPLEMENT_KEY, ControllerMediator.getInstance().isReverseComplementing());
        session.put(HIDE_SINGLE_EXON_ISOFORMS_KEY, ControllerMediator.getInstance().isHidingSingleExonIsoforms());
        session.put(HIDE_DOT_PLOT_KEY, ControllerMediator.getInstance().isHidingDotPlot());
        session.put(SHOW_ISOFORM_PLOT_LEGEND_KEY, ControllerMediator.getInstance().isShowingIsoformPlotLegend());
        session.put(SHOW_MEDIAN_KEY, ControllerMediator.getInstance().isShowingMedian());
        session.put(SHOW_AVERAGE_KEY, ControllerMediator.getInstance().isShowingAverage());
        session.put(INCLUDE_ZEROS_KEY, ControllerMediator.getInstance().isIncludingZeros());
        session.put(SHOW_GENE_NAME_AND_ID_KEY, ControllerMediator.getInstance().isShowingGeneNameAndID());
        session.put(SHOW_GENE_NAME_KEY, ControllerMediator.getInstance().isShowingGeneName());
        session.put(SHOW_GENE_ID_KEY, ControllerMediator.getInstance().isShowingGeneID());
        session.put(SHOW_ISOFORM_NAME_KEY, ControllerMediator.getInstance().isShowingIsoformName());
        session.put(SHOW_ISOFORM_ID_KEY, ControllerMediator.getInstance().isShowingIsoformID());
        session.put(COLOR_CELL_PLOT_BY_ISOFORM_KEY, ControllerMediator.getInstance().isColoringCellPlotBySelectedIsoform());
        session.put(MIN_TPM_KEY, ControllerMediator.getInstance().getGradientMinTPM());
        session.put(MAX_TPM_KEY, ControllerMediator.getInstance().getGradientMaxTPM());
        session.put(MIN_TPM_COLOR_KEY, ControllerMediator.getInstance().getGradientMinColorCode());
        session.put(MID_TPM_COLOR_KEY, ControllerMediator.getInstance().getGradientMidColorCode());
        session.put(MAX_TPM_COLOR_KEY, ControllerMediator.getInstance().getGradientMaxColorCode());
        session.put(TPM_SCALE_KEY, ControllerMediator.getInstance().getScaleOptionInUse());
        session.put(OPTION_FILTERING_BY_KEY, ControllerMediator.getInstance().getOptionFilteringBy());
        session.put(DIS_MIN_TPM_KEY, ControllerMediator.getInstance().getDISMinTPM());
        session.put(DIS_MIN_PERCENT_EXPRESSED_KEY, ControllerMediator.getInstance().getDISMinPercentExpressed());
        session.put(DE_SELECTED_CATEGORIES_KEY, ControllerMediator.getInstance().getDESelectedCategories());
        session.put(DE_MIN_FOLD_CHANGE_KEY, ControllerMediator.getInstance().getDEMinFoldChange());
        session.put(DE_MIN_TPM_KEY, ControllerMediator.getInstance().getDEMinTPM());
        session.put(DE_MIN_PERCENT_EXPRESSED_KEY, ControllerMediator.getInstance().getDEMinPercentExpressed());
        session.put(CSE_SELECTED_CATEGORIES_KEY, ControllerMediator.getInstance().getCSESelectedCategories());
        session.put(CSE_MIN_TPM_KEY, ControllerMediator.getInstance().getCSEMinTPM());
        session.put(CSE_MIN_PERCENT_EXPRESSED_KEY, ControllerMediator.getInstance().getCSEMinPercentExpressed());
        session.put(CSE_MAX_TPM_KEY, ControllerMediator.getInstance().getCSEMaxTPM());
        session.put(CSE_MAX_PERCENT_EXPRESSED_KEY, ControllerMediator.getInstance().getCSEMaxPercentExpressed());
        session.put(USING_UMAP_FOR_EMBEDDING_KEY, ControllerMediator.getInstance().usingUMAPSettings());
        session.put(PERPLEXITY_KEY, ControllerMediator.getInstance().getPerplexity());
        session.put(MAX_ITERATIONS_KEY, ControllerMediator.getInstance().getMaxIterations());
        session.put(MIN_DIST_KEY, ControllerMediator.getInstance().getMinDist());
        session.put(NEAREST_NEIGHBORS_KEY, ControllerMediator.getInstance().getNearestNeighbors());
        session.put(FIGURE_SCALE_KEY, ControllerMediator.getInstance().getFigureScale());
        session.put(FIGURE_TYPE_EXPORTING_KEY, ControllerMediator.getInstance().getFigureTypeExporting());
        return session;
    }
}
