package com.library.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.library.exception.BadRequestException;
import com.library.exception.InternalServerException;
import com.library.exception.NoContentException;
import com.library.exception.NotFoundException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookController {

    // 使用 slf4j 作为日志框架
    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

    @Autowired
    BookRepository bookRepository;

    // API - Get all books with or without given title
    @GetMapping("/books")
    @SecurityRequirement(name = "user")
    public ResponseEntity<List<Book>> getAllBooks(@RequestParam(required = false) String title) {
        List<Book> books = new ArrayList<>();

        if (title == null)
            books.addAll(bookRepository.findAll());
        else
            books.addAll(bookRepository.findByTitleContaining(title));

        if (books.size() == 0) {
            throw new NoContentException("No content");
        }

        ResponseEntity<List<Book>> re = new ResponseEntity<>(books, HttpStatus.OK);
        LOGGER.info(re.toString());
        return re;
    }

    // API - Find book by Id
    @GetMapping("/books/{id}")
    @Cacheable(value="book", key="#id") // 缓存key为id的数据到缓存book中
    @SecurityRequirement(name = "user")
    public ResponseEntity<Book> getBookById(@PathVariable("id") String id) {
        Optional<Book> book;

        try {
            book = bookRepository.findById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid input param :" +id);
        } catch (Exception e) {
            throw new InternalServerException("Unknown error");
        }

        if(!book.isPresent()) {
            throw new NotFoundException("Invalid book id :" + id);
        }

        LOGGER.info("为id、key为{}的book数据做了缓存", id);
        ResponseEntity<Book> re = new ResponseEntity<>(book.get(), HttpStatus.OK);
        LOGGER.info(re.toString());
        return re;
    }

    // API - Create a new book
    @PostMapping("/books")
    @SecurityRequirement(name = "admin")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        if (book.getTitle() == null || book.getAuthor() == null)
            throw new BadRequestException("The created book's title and author could not be null");

        Book _book;

        try {
             _book = bookRepository
                    .save(new Book(book.getTitle(), book.getAuthor(), book.getAmount()));
        } catch (Exception e) {
            throw new InternalServerException("Unknown error");
        }

        ResponseEntity<Book> re = new ResponseEntity<>(_book, HttpStatus.CREATED);
        LOGGER.info(re.toString());
        return re;
    }

    // API - Update the information of a book with given id
    @PutMapping("/books/{id}")
    @SecurityRequirement(name = "admin")
    @CachePut(value="book", key="#book.id") // 缓存更新的数据到缓存，其中缓存名称为book数据的key是book的id
    public ResponseEntity<Book> updateBook(@PathVariable("id") Long id, @RequestBody Book book) {
        if (book.getTitle() == null || book.getAuthor() == null)
            throw new BadRequestException("The created book's title and author could not be null");

        Optional<Book> bookData;

        if(!bookRepository.existsById(id))
            throw new NotFoundException("Invalid book id :" + id);

        bookData = bookRepository.findById(id);

        if(!bookData.isPresent())
            throw new InternalServerException("Unknown error");

        Book _book = bookData.get();

        _book.setTitle(book.getTitle());
        _book.setAuthor(book.getAuthor());
        _book.setAmount(book.getAmount());

        LOGGER.info("为id、key为{}的book数据做了缓存", id);

        ResponseEntity<Book> re = new ResponseEntity<>(_book, HttpStatus.OK);
        LOGGER.info(re.toString());
        return re;
    }

    // API - Delete a book with given id
    @DeleteMapping("/books/{id}")
    @SecurityRequirement(name = "admin")
    @CacheEvict(value="book") // 从缓存book中删除key为id的数据
    public ResponseEntity<HttpStatus> deleteBook(@PathVariable("id") long id) {

        if(!bookRepository.existsById(id))
            throw new NotFoundException("Invalid book id :" + id);

        try {
            bookRepository.deleteById(id);
        } catch (Exception e) {
            throw new InternalServerException("Unknown error");
        }

        LOGGER.info("删除了id、key为{}的book数据缓存",id);

        ResponseEntity<HttpStatus> re = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        LOGGER.info(re.toString());
        return re;
    }

    // API - Delete all books
    @DeleteMapping("/books")
    @SecurityRequirement(name = "admin")
    @CacheEvict(cacheNames = "book",allEntries = true)
    public ResponseEntity<HttpStatus> deleteAllBooks() {

        try {
            bookRepository.deleteAll();
        } catch (Exception e) {
            throw new InternalServerException("Unknown error");
        }

        LOGGER.info("删除了所有book数据缓存");

        ResponseEntity<HttpStatus> re = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        LOGGER.info(re.toString());
        return re;
    }
}