package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.domain.Beer;
import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @MockBean
    BeerOrderService beerOrderService;

    @Autowired
    MockMvc mockMvc;

    BeerOrderDto validBeerOrder;

    BeerOrderLineDto beerOrderLineDto;

    @Captor
    ArgumentCaptor<UUID> customerIdCaptor;

    @Captor
    ArgumentCaptor<UUID> orderIdCaptor;


    @BeforeEach
    void setUp(){
        beerOrderLineDto = BeerOrderLineDto.builder()
                        .id(UUID.randomUUID())
                        .version(1)
                        .createdDate(OffsetDateTime.now())
                        .lastModifiedDate(OffsetDateTime.now())
                        .beerId(UUID.randomUUID())
                        .orderQuantity(50)
                        .build();

        validBeerOrder = BeerOrderDto.builder()
                        .version(1)
                        .id(UUID.randomUUID())
                        .customerId(UUID.randomUUID())
                        .beerOrderLines(Arrays.asList(beerOrderLineDto))
                        .orderStatus(OrderStatusEnum.NEW)
                        .orderStatusCallbackUrl("http://hogehoge.com")
                        .customerRef("reference")
                        .createdDate(OffsetDateTime.now())
                        .lastModifiedDate(OffsetDateTime.now())
                        .build();

    }

    @AfterEach
    void tearDown(){
        reset(beerOrderService);
    }

    @Test
    void getOrder() throws Exception {
        given(beerOrderService.getOrderById(customerIdCaptor.capture(), orderIdCaptor.capture())).willReturn(validBeerOrder);

        MvcResult result = mockMvc.perform(get("/api/v1/customers/"+validBeerOrder.getCustomerId()+"/orders/"+validBeerOrder.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id", is(validBeerOrder.getId().toString())))
                        .andExpect(jsonPath("$.customerId", is(validBeerOrder.getCustomerId().toString())))
                        .andExpect(jsonPath("$.orderStatus", is(validBeerOrder.getOrderStatus().toString())))
                        .andExpect(jsonPath("$.orderStatusCallbackUrl", is(validBeerOrder.getOrderStatusCallbackUrl())))
                        .andExpect(jsonPath("$.customerRef", is(validBeerOrder.getCustomerRef())))
                        .andExpect(jsonPath("$.beerOrderLines[0].beerId", is(validBeerOrder.getBeerOrderLines().get(0).getBeerId().toString())))
                       .andReturn();

        assertThat(orderIdCaptor.getValue()).isEqualTo(validBeerOrder.getId());
        assertThat(customerIdCaptor.getValue()).isEqualTo(validBeerOrder.getCustomerId());
        System.out.println(result.getResponse().getContentAsString());
    }

    @DisplayName("list ops ")
    @Nested
    public class TestListOperation {
        @Captor
        ArgumentCaptor<String> beerNameCaptor;

        @Captor
        ArgumentCaptor<UUID> customerIdCaptor;

        @Captor
        ArgumentCaptor<PageRequest> pageRequestCaptor;

        BeerOrderPagedList beerOrderPagedList;

        @BeforeEach
        void setUp(){
            List<BeerOrderDto> beerOrder = new ArrayList<>();
            beerOrder.add(validBeerOrder);
            beerOrder.add(BeerOrderDto.builder()
                    .version(1)
                    .id(UUID.randomUUID())
                    .customerId(UUID.randomUUID())
                    .beerOrderLines(Arrays.asList(beerOrderLineDto))
                    .orderStatus(OrderStatusEnum.NEW)
                    .orderStatusCallbackUrl("http://fugafuga.com")
                    .customerRef("fugafuga")
                    .createdDate(OffsetDateTime.now())
                    .lastModifiedDate(OffsetDateTime.now())
                    .build());
            beerOrderPagedList = new BeerOrderPagedList(beerOrder, PageRequest.of(1, 1), 1l);
            given(beerOrderService.listOrders(customerIdCaptor.capture(), pageRequestCaptor.capture())).willReturn(beerOrderPagedList);
        }

        @Test
        void listOrders() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/v1/customers/"+ validBeerOrder.getCustomerId() +"/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
//                    .andExpect(jsonPath("$.content[0].id"), is());
                    .andReturn();

            System.out.println(result.getResponse().getContentAsString());
        }
    }


}