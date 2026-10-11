package com.bidly.service;

import com.bidly.dao.CategoryDao;
import com.bidly.interceptor.Logged;
import com.bidly.model.Category;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

@Stateless
@Logged
public class CategoryService {

    @EJB
    private CategoryDao categoryDao;

    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    public Category getCategory(Long id) {
        return categoryDao.findById(id);
    }
}
