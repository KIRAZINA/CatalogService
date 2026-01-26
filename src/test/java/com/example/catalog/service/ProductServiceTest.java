package com.example.catalog.service;

import com.example.catalog.dto.ProductCreateDto;
import com.example.catalog.dto.ProductDto;
import com.example.catalog.dto.ProductUpdateDto;
import com.example.catalog.entity.Product;
import com.example.catalog.document.ProductDocument;
import com.example.catalog.event.ContentIndexEvent;
import com.example.catalog.mapper.ProductMapper;
import com.example.catalog.repository.ProductRepository;
import com.example.catalog.repository.elasticsearch.ProductElasticsearchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private com.example.catalog.repository.CategoryRepository categoryRepository;

    @Mock
    private com.example.catalog.repository.TagRepository tagRepository;

    @Mock
    private ProductElasticsearchRepository elasticsearchRepository;

    @Mock
    private ProductMapper mapper;

    @Mock
    private KafkaTemplate<String, ContentIndexEvent> kafkaTemplate;

    @InjectMocks
    private ProductService service;

    @Test
    void getById() {
        UUID id = UUID.randomUUID();
        Product product = new Product();
        product.setId(id);
        ProductDto dto = new ProductDto();
        dto.setId(id);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(mapper.toDto(product)).thenReturn(dto);

        ProductDto result = service.getById(id);
        assertEquals(id, result.getId());
    }

    @Test
    void getById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getById(id));
    }

    @Test
    void create() {
        ProductCreateDto createDto = new ProductCreateDto();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());

        ProductDocument document = new ProductDocument();

        when(mapper.toEntity(createDto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDto(product)).thenReturn(dto);
        when(mapper.toDocument(product)).thenReturn(document);

        service.create(createDto);

        verify(elasticsearchRepository).save(any());
        ArgumentCaptor<ContentIndexEvent> eventCaptor = ArgumentCaptor.forClass(ContentIndexEvent.class);
        verify(kafkaTemplate).send(eq("topic.content.index"), eventCaptor.capture());
        assertEquals("create", eventCaptor.getValue().getAction());
    }

    @Test
    void update() {
        UUID id = UUID.randomUUID();
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setTitle("Updated");

        Product existing = new Product();
        existing.setId(id);
        Product saved = new Product();
        saved.setId(id);

        ProductDto mapped = new ProductDto();
        mapped.setId(id);
        mapped.setTitle("Updated");

        ProductDocument document = new ProductDocument();
        document.setId(id.toString());

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(mapped);
        when(mapper.toDocument(saved)).thenReturn(document);

        service.update(id, updateDto);

        verify(elasticsearchRepository).save(any());
        ArgumentCaptor<ContentIndexEvent> eventCaptor = ArgumentCaptor.forClass(ContentIndexEvent.class);
        verify(kafkaTemplate).send(eq("topic.content.index"), eventCaptor.capture());
        assertEquals("update", eventCaptor.getValue().getAction());
    }

    @Test
    void update_notFound_throws() {
        UUID id = UUID.randomUUID();
        ProductUpdateDto updateDto = new ProductUpdateDto();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.update(id, updateDto));
    }

    @Test
    void delete() {
        UUID id = UUID.randomUUID();
        Product existing = new Product();
        existing.setId(id);

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(productRepository).delete(existing);
        verify(elasticsearchRepository).deleteById(eq(id.toString()));
        ArgumentCaptor<ContentIndexEvent> eventCaptor = ArgumentCaptor.forClass(ContentIndexEvent.class);
        verify(kafkaTemplate).send(eq("topic.content.index"), eventCaptor.capture());
        assertEquals("delete", eventCaptor.getValue().getAction());
    }

    @Test
    void delete_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.delete(id));
    }

    @Test
    void create_withoutKafkaAndElasticsearch_doesNotFail() {
        ReflectionTestUtils.setField(service, "kafkaTemplate", null);
        ReflectionTestUtils.setField(service, "elasticsearchRepository", null);

        ProductCreateDto createDto = new ProductCreateDto();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());

        when(mapper.toEntity(createDto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDto(product)).thenReturn(dto);
        when(mapper.toDocument(product)).thenReturn(new ProductDocument());

        ProductDto result = service.create(createDto);

        assertEquals(product.getId(), result.getId());
        verifyNoInteractions(kafkaTemplate);
        verifyNoInteractions(elasticsearchRepository);
    }

    @Test
    void update_withoutKafkaAndElasticsearch_doesNotFail() {
        ReflectionTestUtils.setField(service, "kafkaTemplate", null);
        ReflectionTestUtils.setField(service, "elasticsearchRepository", null);

        UUID id = UUID.randomUUID();
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setTitle("Updated");

        Product existing = new Product();
        existing.setId(id);
        Product saved = new Product();
        saved.setId(id);

        ProductDto mapped = new ProductDto();
        mapped.setId(id);
        mapped.setTitle("Updated");

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(mapped);
        when(mapper.toDocument(saved)).thenReturn(new ProductDocument());

        ProductDto result = service.update(id, updateDto);

        assertEquals(id, result.getId());
        verifyNoInteractions(kafkaTemplate);
        verifyNoInteractions(elasticsearchRepository);
    }

    @Test
    void delete_withoutKafkaAndElasticsearch_doesNotFail() {
        ReflectionTestUtils.setField(service, "kafkaTemplate", null);
        ReflectionTestUtils.setField(service, "elasticsearchRepository", null);

        UUID id = UUID.randomUUID();
        Product existing = new Product();
        existing.setId(id);

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(productRepository).delete(existing);
        verifyNoInteractions(kafkaTemplate);
        verifyNoInteractions(elasticsearchRepository);
    }

    @Test
    void search_withoutElasticsearch_fallsBackToFindAll() {
        ReflectionTestUtils.setField(service, "elasticsearchRepository", null);

        Pageable pageable = Pageable.ofSize(10);
        Page<Product> products = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(products);

        Page<ProductDto> result = service.search("q", pageable);

        assertEquals(0, result.getTotalElements());
        verify(productRepository).findAll(any(Pageable.class));
        verifyNoInteractions(kafkaTemplate);
    }
}