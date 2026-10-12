
create table audit_logs (
                            created_at timestamp(6) with time zone not null,
                            updated_at timestamp(6) with time zone not null,
                            id uuid not null,
                            user_id uuid not null,
                            description varchar(350) not null,
                            action varchar(255) not null check ((action in ('REGISTER','LOCAL_LOGIN','OAUTH_LOGIN','PASSKEY_LOGIN','LOGIN_FAILURE','LOGIN_ATTEMPT','LOGOUT','PASSWORD_RESET','EMAIL_VERIFIED','EMAIL_CHANGED','ACCOUNT_SOFT_DELETED','ACCOUNT_HARD_DELETED','SOCIAL_LINK','SOCIAL_UNLINK','PASSKEY_LINK'))),
                            email varchar(255) not null,
                            full_name varchar(255) not null,
                            ip_address varchar(255),
                            user_agent varchar(255),
                            metadata jsonb,
                            primary key (id)
);

create table passkeys (
                          created_at timestamp(6) with time zone not null,
                          last_used timestamp(6) with time zone,
                          signature_count bigint not null,
                          updated_at timestamp(6) with time zone not null,
                          id uuid not null,
                          user_id uuid not null unique,
                          attestation_client_datajson TEXT,
                          attestation_object TEXT,
                          credential_id varchar(255) not null unique,
                          label varchar(255),
                          public_key TEXT not null,
                          primary key (id)
);

create table refresh_tokens (
                                revoked boolean not null,
                                created_at timestamp(6) with time zone,
                                expires_at timestamp(6) with time zone not null,
                                revoked_at timestamp(6) with time zone,
                                id uuid not null,
                                user_id uuid not null,
                                token_hash varchar(255) not null unique,
                                primary key (id)
);

create table users (
                       email_verified boolean not null,
                       failed_login_attempts integer not null,
                       is_locked boolean not null,
                       created_at timestamp(6) with time zone not null,
                       deleted_at timestamp(6) with time zone,
                       locked_until timestamp(6) with time zone,
                       updated_at timestamp(6) with time zone not null,
                       id uuid not null,
                       email varchar(255) not null unique,
                       first_name varchar(255),
                       last_name varchar(255),
                       password varchar(255),
                       picture varchar(255),
                       provider varchar(255),
                       primary key (id)
);

alter table if exists refresh_tokens
    add constraint FK1lih5y2npsf8u5o3vhdb9y0os
    foreign key (user_id)
    references users;

create table audit_logs (
                            created_at timestamp(6) with time zone not null,
                            updated_at timestamp(6) with time zone not null,
                            id uuid not null,
                            user_id uuid not null,
                            description varchar(350) not null,
                            action varchar(255) not null check ((action in ('REGISTER','LOCAL_LOGIN','OAUTH_LOGIN','PASSKEY_LOGIN','LOGIN_FAILURE','LOGIN_ATTEMPT','LOGOUT','PASSWORD_RESET','EMAIL_VERIFIED','EMAIL_CHANGED','ACCOUNT_SOFT_DELETED','ACCOUNT_HARD_DELETED','SOCIAL_LINK','SOCIAL_UNLINK','PASSKEY_LINK'))),
                            email varchar(255) not null,
                            full_name varchar(255) not null,
                            ip_address varchar(255),
                            user_agent varchar(255),
                            metadata jsonb,
                            primary key (id)
);

create table passkeys (
                          created_at timestamp(6) with time zone not null,
                          last_used timestamp(6) with time zone,
                          signature_count bigint not null,
                          updated_at timestamp(6) with time zone not null,
                          id uuid not null,
                          user_id uuid not null unique,
                          attestation_client_datajson TEXT,
                          attestation_object TEXT,
                          credential_id varchar(255) not null unique,
                          label varchar(255),
                          public_key TEXT not null,
                          primary key (id)
);

create table refresh_tokens (
                                revoked boolean not null,
                                created_at timestamp(6) with time zone,
                                expires_at timestamp(6) with time zone not null,
                                revoked_at timestamp(6) with time zone,
                                id uuid not null,
                                user_id uuid not null,
                                token_hash varchar(255) not null unique,
                                primary key (id)
);

create table users (
                       email_verified boolean not null,
                       failed_login_attempts integer not null,
                       is_locked boolean not null,
                       created_at timestamp(6) with time zone not null,
                       deleted_at timestamp(6) with time zone,
                       locked_until timestamp(6) with time zone,
                       updated_at timestamp(6) with time zone not null,
                       id uuid not null,
                       email varchar(255) not null unique,
                       first_name varchar(255),
                       last_name varchar(255),
                       password varchar(255),
                       picture varchar(255),
                       provider varchar(255),
                       primary key (id)
);

alter table if exists refresh_tokens
    add constraint FK1lih5y2npsf8u5o3vhdb9y0os
    foreign key (user_id)
    references users;
