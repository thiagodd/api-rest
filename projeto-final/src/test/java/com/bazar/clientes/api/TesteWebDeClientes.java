package com.bazar.clientes.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.clientes.dominio.EnderecoRepository;
import com.bazar.clientes.dominio.EnderecoService;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;

/**
 * A camada web dos clientes, com os repositórios em memória de verdade. As classes que usam esta anotação
 * compartilham um único contexto (11.04).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebMvcTest({ ClienteController.class, EnderecoController.class })
@Import({ ClienteRepository.class, EnderecoRepository.class, EnderecoService.class })
public @interface TesteWebDeClientes {
}
