package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

/**
 * JdbcTemplate
 */
@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "INSERT INTO ITEM (ITEM_NAME, PRICE, QUANTITY) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setDouble(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);

        long key = keyHolder.getKey().longValue();
        item.setId(key);

        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql ="UPDATE ITEM SET ITEM_NAME=?, PRICE=?, QUANTITY=? WHERE ID = ?";
        template.update(sql, updateParam.getItemName(), updateParam.getPrice(), updateParam.getQuantity(), itemId);

    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "SELECT ID, ITEM_NAME, PRICE, QUANTITY FROM ITEM WHERE ID = ?";
        try {
            Item item = template.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } /*catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }*/
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "SELECT ID, ITEM_NAME, PRICE, QUANTITY FROM ITEM ";
        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null){
            sql += " WHERE";
        }

        boolean andFlag = false;
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(itemName)){
            sql +=" ITEM_NAME LIKE CONCAT('%', ?, '%')";
            params.add(itemName);
            andFlag = true;
        }

        if  (maxPrice != null){
            if (andFlag){
                sql += " AND";
            }
            sql += " PRICE <= ?";
            params.add(maxPrice);
        }

        log.info("sql = {}", sql);

        return template.query(sql, itemRowMapper(), params.toArray());
    }

    private RowMapper<Item> itemRowMapper() {
        return ((rs, nowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("ID"));
            item.setItemName(rs.getString("ITEM_NAME"));
            item.setPrice(rs.getInt("PRICE"));
            item.setQuantity(rs.getInt("QUANTITY"));
            return item;
        });

        //return BeanPropertyRowMapper.newInstance(Item.class);
    }
}
