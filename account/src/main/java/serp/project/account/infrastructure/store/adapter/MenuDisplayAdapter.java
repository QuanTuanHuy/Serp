/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.GetMenuDisplayParams;
import serp.project.account.core.domain.entity.MenuDisplayEntity;
import serp.project.account.core.port.store.IMenuDisplayPort;
import serp.project.account.infrastructure.store.mapper.MenuDisplayMapper;
import serp.project.account.infrastructure.store.model.MenuDisplayModel;
import serp.project.account.infrastructure.store.repository.IMenuDisplayRepository;
import serp.project.account.kernel.utils.CollectionUtils;

@Component
@RequiredArgsConstructor
public class MenuDisplayAdapter implements IMenuDisplayPort {
    private final IMenuDisplayRepository menuDisplayRepository;
    private final MenuDisplayMapper menuDisplayMapper;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public MenuDisplayEntity save(MenuDisplayEntity menuDisplay) {
        MenuDisplayModel menuDisplayModel = menuDisplayMapper.toModel(menuDisplay);
        return menuDisplayMapper.toEntity(menuDisplayRepository.save(menuDisplayModel));
    }

    @Override
    public List<MenuDisplayEntity> getByIds(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids) && ids.size() == 1) {
            return List.of(getById(ids.getFirst()));
        }
        return menuDisplayMapper.toEntityList(menuDisplayRepository.findByIdIn(ids));
    }

    private MenuDisplayEntity getById(Long id) {
        return menuDisplayMapper.toEntity(menuDisplayRepository.findById(id).orElse(null));
    }

    @Override
    public List<MenuDisplayEntity> getByModuleId(Long moduleId) {
        return menuDisplayMapper.toEntityList(menuDisplayRepository.findByModuleId(moduleId));
    }

    @Override
    public MenuDisplayEntity getByModuleIdAndName(Long moduleId, String name) {
        return menuDisplayMapper.toEntity(menuDisplayRepository.findByModuleIdAndName(moduleId, name));
    }

    @Override
    public void deleteMenuDisplay(Long id) {
        menuDisplayRepository.deleteById(id);
    }

    @Override
    public Pair<List<MenuDisplayEntity>, Long> getAllMenuDisplays(GetMenuDisplayParams params) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM menu_displays WHERE 1=1");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM menu_displays WHERE 1=1");
        StringBuilder whereClause = new StringBuilder();

        if (params.getModuleId() != null) {
            whereClause.append(" AND module_id = ? ");
        }
        sql.append(whereClause);
        countSql.append(whereClause);

        String sortBy = params.getSortBy();
        String sortDirection = params.getSortDirection();
        if (!List.of("id", "name", "order_index", "created_at", "updated_at").contains(sortBy)) {
            sortBy = "id";
        }
        sql.append(" ORDER BY ").append(sortBy).append(" ").append(sortDirection);
        int page = params.getPage();
        int pageSize = params.getPageSize();
        int offset = page * pageSize;
        sql.append(" LIMIT ? OFFSET ? ");

        List<Object> whereArgs = new ArrayList<>();
        if (params.getModuleId() != null) {
            whereArgs.add(params.getModuleId());
        }

        Long total = jdbcTemplate.query(con -> {
            var ps = con.prepareStatement(countSql.toString());
            for (int i = 0; i < whereArgs.size(); i++) {
                ps.setObject(i + 1, whereArgs.get(i));
            }
            return ps;
        }, rs -> rs.next() ? rs.getLong(1) : 0L);

        List<Object> selectArgs = new ArrayList<>(whereArgs);
        selectArgs.add(pageSize);
        selectArgs.add(offset);

        List<MenuDisplayEntity> result = jdbcTemplate.query(con -> {
            var ps = con.prepareStatement(sql.toString());
            for (int i = 0; i < selectArgs.size(); i++) {
                ps.setObject(i + 1, selectArgs.get(i));
            }
            return ps;
        }, (rs, rowNum) -> {
            MenuDisplayModel model = MenuDisplayModel.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .path(rs.getString("path"))
                    .icon(rs.getString("icon"))
                    .order(rs.getObject("order_index") != null ? rs.getInt("order_index") : null)
                    .parentId(rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null)
                    .moduleId(rs.getLong("module_id"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .build();
            return menuDisplayMapper.toEntity(model);
        });

        return Pair.of(result, total);
    }
}
