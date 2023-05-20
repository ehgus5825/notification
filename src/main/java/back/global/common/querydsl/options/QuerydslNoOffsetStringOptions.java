package back.global.common.querydsl.options;

import back.global.common.querydsl.expression.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;


import javax.annotation.Nonnull;

public class QuerydslNoOffsetStringOptions<T> extends QuerydslNoOffsetOptions<T> {

    // 현재 id

    private String currentId;

    // 마지막 id

    private String lastId;

    // 테이블 필드 (querydsl)

    private final StringPath field;

    // 필드명, expression 을 받아서 초기화

    public QuerydslNoOffsetStringOptions(@Nonnull StringPath field,
                                         @Nonnull Expression expression) {
        super(field, expression);
        this.field = field;
    }

    // 현재 ID 반환

    public String getCurrentId() {
        return currentId;
    }

    // 마지막 id 반환

    public String getLastId() {
        return lastId;
    }

    // 내부적으로 첫번째 키와 마지막 키를 찾아냄

    @Override
    public void initKeys(JPAQuery<T> query, int page) {
        if(page == 0) {
            initFirstId(query);
            initLastId(query);

            if (logger.isDebugEnabled()) {
                logger.debug("First Key= "+currentId+", Last Key= "+ lastId);
            }
        }
    }

    // 첫번째 키를 찾아냄

    @Override
    protected void initFirstId(JPAQuery<T> query) {
        JPAQuery<T> clone = query.clone();

        // group by가 있는지 확인
        boolean isGroupByQuery = isGroupByQuery(clone);

        if(isGroupByQuery) {
            // group by가 없다면 asc나 desc에 따라서 키를 찾음

            currentId = clone
                    .select(field)
                    .orderBy(expression.isAsc()? field.asc() : field.desc())
                    .fetchFirst();
        } else {
            // group by가 있다면 최솟값과 최댓값으로 키를 찾음

            currentId = clone
                    .select(expression.isAsc()? field.min(): field.max())
                    .fetchFirst();
        }

    }

    // 마지막 키를 찾아냄

    @Override
    protected void initLastId(JPAQuery<T> query) {
        JPAQuery<T> clone = query.clone();
        boolean isGroupByQuery = isGroupByQuery(clone);

        // 위와 동일하지만 방향이 반대임

        if(isGroupByQuery) {
            lastId = clone
                    .select(field)
                    .orderBy(expression.isAsc()? field.desc() : field.asc())
                    .fetchFirst();
        } else {
            lastId = clone
                    .select(expression.isAsc()? field.max(): field.min())
                    .fetchFirst();
        }
    }

    // 최종 query 를 만듬

    @Override
    public JPAQuery<T> createQuery(JPAQuery<T> query, int page) {

        // 첫번째 키가 없다는 것은 조건식을 부여할 수 없다.

        if (currentId == null) {
            return query;
        }

        // 그게 아니면 조건식을 부여할 수 있다.

        return query
                .where(whereExpression(page))
                .orderBy(orderExpression());
    }

    private BooleanExpression whereExpression(int page) {
        return expression.where(field, page, currentId)
                .and(expression.isAsc()? field.loe(lastId) : field.goe(lastId));
    }

    private OrderSpecifier<String> orderExpression() {
        return expression.order(field);
    }

    @Override
    public void resetCurrentId(T item) {
        currentId = (String) getFiledValue(item);
        if (logger.isDebugEnabled()) {
            logger.debug("Current Select Key= " + currentId);
        }
    }
}
