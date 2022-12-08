package com.library.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        ResponseEntity<List<Book>> re = new ResponseEntity<>(HttpStatus.NO_CONTENT);

        try {
            List<Book> books = new ArrayList<>();

            if (title == null)
                books.addAll(bookRepository.findAll());
            else
                books.addAll(bookRepository.findByTitleContaining(title));

            if (books.isEmpty()) {
                LOGGER.info(re.toString());
                return re;
            }

            re = new ResponseEntity<>(books, HttpStatus.OK);
            LOGGER.info(re.toString());
            return re;
        } catch (Exception e) {
            re = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error(re.toString());
            return re;
        }
    }

    // API - Find book by Id
    @GetMapping("/books/{id}")
    @Cacheable(value="book", key="#id") // 缓存key为id的数据到缓存book中
    @SecurityRequirement(name = "user")
    public ResponseEntity<Book> getBookById(@PathVariable("id") long id) {
        ResponseEntity<Book> re;

        Optional<Book> bookData = bookRepository.findById(id);

        if(bookData.isPresent()) {
            LOGGER.info("为id、key为{}的book数据做了缓存", id);
            re = new ResponseEntity<>(bookData.get(), HttpStatus.OK);
        } else {
            re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LOGGER.info(re.toString());
        return re;
    }

    // API - Create a new book
    @PostMapping("/books")
    @SecurityRequirement(name = "admin")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        ResponseEntity<Book> re;

        try {
            Book _book = bookRepository
                    .save(new Book(book.getTitle(), book.getAuthor(), book.getAmount()));

            re = new ResponseEntity<>(_book, HttpStatus.CREATED);
            LOGGER.info(re.toString());
            return re;
        } catch (Exception e) {
            re = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error(re.toString());
            return re;
        }
    }

    // API - Update the information of a book with given id
    @PutMapping("/books/{id}")
    @SecurityRequirement(name = "admin")
    @CachePut(value="book", key="#book.id") // 缓存更新的数据到缓存，其中缓存名称为book数据的key是book的id
    public ResponseEntity<Book> updateBook(@PathVariable("id") long id, @RequestBody Book book) {
        ResponseEntity<Book> re;

        Optional<Book> bookData = bookRepository.findById(id);

        if (bookData.isPresent()) {
            Book _book = bookData.get();

            _book.setTitle(book.getTitle());
            _book.setAuthor(book.getAuthor());
            _book.setAmount(book.getAmount());

            LOGGER.info("为id、key为{}的book数据做了缓存", id);

            re = new ResponseEntity<>(bookRepository.save(_book), HttpStatus.OK);
            LOGGER.info(re.toString());
        } else {
            re = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            LOGGER.error(re.toString());
        }

        return re;
    }

    // API - Delete a book with given id
    @DeleteMapping("/books/{id}")
    @SecurityRequirement(name = "admin")
    @CacheEvict(value="book") // 从缓存book中删除key为id的数据
    public ResponseEntity<HttpStatus> deleteBook(@PathVariable("id") long id) {
        ResponseEntity<HttpStatus> re;
        try {
            bookRepository.deleteById(id);

            LOGGER.info("删除了id、key为{}的book数据缓存",id);

            re = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            LOGGER.info(re.toString());
            return re;
        } catch (Exception e) {
            re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error(re.toString());
            return re;
        }
    }

    // API - Delete all books
    @DeleteMapping("/books")
    @SecurityRequirement(name = "admin")
    public ResponseEntity<HttpStatus> deleteAllBooks() {
        ResponseEntity<HttpStatus> re;
        try {
            bookRepository.deleteAll();
            re = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            LOGGER.error(re.toString());
            return re;
        } catch (Exception e) {
            re = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            LOGGER.error(re.toString());
            return re;
        }
    }
}