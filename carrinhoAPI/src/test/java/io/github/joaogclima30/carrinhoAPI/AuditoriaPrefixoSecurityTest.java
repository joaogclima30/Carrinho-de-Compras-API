package io.github.joaogclima30.carrinhoAPI;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "api.prefix=/auditoria")
@ActiveProfiles("test")
class AuditoriaPrefixoSecurityTest {
    @Autowired WebApplicationContext context;

    @Test void alterarPrefixoConfiguravelNaoPodeRemoverProtecaoDoCarrinho() throws Exception {
        MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build()
                .perform(get("/auditoria/carrinhos/999999/total"))
                .andExpect(status().isUnauthorized());
    }
}
