package com.example.catalog.controller;

import com.example.catalog.CatalogServiceApplication;
import com.example.catalog.dto.ProductDto;
import com.example.catalog.entity.ProductType;
import com.example.catalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CatalogServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService service;

    @Test
    void getCatalog_noParams_callsFindAll() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
        when(service.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk());

        verify(service).findAll(any(Pageable.class));
    }

    @Test
    void getCatalog_query_callsSearch() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
        when(service.search(eq("abc"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/catalog").param("query", "abc"))
                .andExpect(status().isOk());

        verify(service).search(eq("abc"), any(Pageable.class));
    }

    @Test
    void getCatalog_type_callsFindByType() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
        when(service.findByType(eq(ProductType.EBOOK), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/catalog").param("type", "EBOOK"))
                .andExpect(status().isOk());

        verify(service).findByType(eq(ProductType.EBOOK), any(Pageable.class));
    }

    @Test
    void getCatalog_priceRange_callsFindByPriceRange() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
        when(service.findByPriceRange(eq(10L), eq(20L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/catalog")
                        .param("minPrice", "10")
                        .param("maxPrice", "20"))
                .andExpect(status().isOk());

        verify(service).findByPriceRange(eq(10L), eq(20L), any(Pageable.class));
    }

    @Test
    void getCatalog_category_callsFindByCategory() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
        when(service.findByCategory(eq("genre"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/catalog").param("category", "genre"))
                .andExpect(status().isOk());

        verify(service).findByCategory(eq("genre"), any(Pageable.class));
    }

    @Test
    void getCatalog_queryTakesPrecedenceOverType() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
        when(service.search(eq("abc"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/catalog")
                        .param("query", "abc")
                        .param("type", "EBOOK"))
                .andExpect(status().isOk());

        verify(service).search(eq("abc"), any(Pageable.class));
    }

    @Test
    void getCatalog_onlyMinPrice_fallsBackToFindAll() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
        when(service.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/catalog").param("minPrice", "10"))
                .andExpect(status().isOk());

        verify(service).findAll(any(Pageable.class));
    }

    @Test
    void getCatalog_onlyMaxPrice_fallsBackToFindAll() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
        when(service.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/catalog").param("maxPrice", "20"))
                .andExpect(status().isOk());

        verify(service).findAll(any(Pageable.class));
    }

    @Test
    void getCatalog_invalidType_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/catalog").param("type", "NOT_A_REAL_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_callsService() throws Exception {
        UUID id = UUID.randomUUID();
        ProductDto dto = new ProductDto();
        dto.setId(id);
        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/catalog/{id}", id))
                .andExpect(status().isOk());

        verify(service).getById(id);
    }

    @Test
    void create_callsService() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setTitle("Test");
        dto.setType(ProductType.EBOOK);
        dto.setCurrency("USD");
        dto.setPriceCents(100);

        ProductDto result = new ProductDto();
        result.setId(UUID.randomUUID());
        when(service.create(any(ProductDto.class))).thenReturn(result);

        mockMvc.perform(post("/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(service).create(any(ProductDto.class));
    }

    @Test
    void update_callsService() throws Exception {
        UUID id = UUID.randomUUID();

        ProductDto dto = new ProductDto();
        dto.setTitle("Updated");
        dto.setType(ProductType.EBOOK);
        dto.setCurrency("USD");
        dto.setPriceCents(200);

        ProductDto result = new ProductDto();
        result.setId(id);
        when(service.update(eq(id), any(ProductDto.class))).thenReturn(result);

        mockMvc.perform(put("/catalog/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(service).update(eq(id), any(ProductDto.class));
    }

    @Test
    void delete_callsService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/catalog/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}
