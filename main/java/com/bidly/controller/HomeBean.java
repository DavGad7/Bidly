package com.bidly.controller;

import com.bidly.model.Auction;
import com.bidly.model.Category;
import com.bidly.service.AuctionService;
import com.bidly.service.CategoryService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class HomeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE = 12;

    @EJB
    private AuctionService auctionService;

    @EJB
    private CategoryService categoryService;

    @Inject
    private NavigationBean navigationBean;

    private List<Auction> auctions;
    private List<Category> categories;
    private String searchKeyword;
    private Long selectedCategoryId;
    private String sortBy = "ENDING_SOON";
    private int currentPage = 1;
    private int totalPages = 1;
    private long totalResults;

    @PostConstruct
    public void init() {
        categories = categoryService.getAllCategories();
        search();
    }

    public void search() {
        currentPage = 1;
        executeSearch();
    }

    public void clearFilters() {
        searchKeyword = null;
        selectedCategoryId = null;
        sortBy = "ENDING_SOON";
        currentPage = 1;
        executeSearch();
    }

    public void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            executeSearch();
        }
    }

    public void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            executeSearch();
        }
    }

    private void executeSearch() {
        auctions = auctionService.searchAuctions(
                searchKeyword, selectedCategoryId, sortBy, currentPage, PAGE_SIZE);

        totalResults = auctionService.countSearchResults(searchKeyword, selectedCategoryId);
        totalPages = Math.max(1, (int) Math.ceil((double) totalResults / PAGE_SIZE));
    }

    public void deleteAuction(Long auctionId) {
        auctionService.deleteAuction(auctionId, navigationBean.getUserId());
        executeSearch();
    }

    public String getThumbnailUrl(Auction auction) {
        if (auction != null && auction.getThumbnailPhotoId() != null) {
            String ctx = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
            return ctx + "/image?id=" + auction.getThumbnailPhotoId();
        }
        return "";
    }

    public boolean isHasNextPage() {
        return currentPage < totalPages;
    }

    public boolean isHasPreviousPage() {
        return currentPage > 1;
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public Long getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(Long selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalResults() {
        return totalResults;
    }
}
