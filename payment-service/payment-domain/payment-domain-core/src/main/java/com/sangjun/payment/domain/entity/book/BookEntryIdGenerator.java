package com.sangjun.payment.domain.entity.book;

import com.sangjun.payment.domain.valueobject.book.BookEntryId;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.InitCommand;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;


public class BookEntryIdGenerator implements IdentifierGenerator {
    public static final String TYPE = "type";
    public static final String SEQUENCE_NAME = "seq_name";

    private String type;
    private String sequenceName;

    private final AtomicLong atomicLong = new AtomicLong(1L);

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) throws HibernateException {
        return new BookEntryId(switch (type) {
            case "SEQUENCE" -> getIdFromSequenceGenerator(session, obj);
            default -> atomicLong.getAndIncrement();
        });
    }

    private Long getIdFromSequenceGenerator(SharedSessionContractImplementor session, Object obj) {
        String sql = String.format("SELECT nextval('%s');", sequenceName);

        Long nextId = null;
        try (Connection con = session.getJdbcConnectionAccess().obtainConnection()) {
            CallableStatement cs = con.prepareCall(sql);
            cs.executeQuery();
            ResultSet rs = cs.getResultSet();

            if (rs.next()) {
                nextId = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new HibernateException(e);
        }

        return Objects.requireNonNull(nextId, String.format("while getting nextId from %s", sequenceName));
    }

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        this.type = ConfigurationHelper.getString(TYPE, params);
        this.sequenceName = ConfigurationHelper.getString(SEQUENCE_NAME, params);
    }

    @Override
    public void registerExportables(Database database) {
        String createSequenceSQL = String.format("CREATE SEQUENCE IF NOT EXISTS %s AS BIGINT" +
                " START WITH 1 INCREMENT BY 1;", sequenceName);
        InitCommand initCommand = new InitCommand(createSequenceSQL);
        database.addInitCommand(initCommand);
    }
}
