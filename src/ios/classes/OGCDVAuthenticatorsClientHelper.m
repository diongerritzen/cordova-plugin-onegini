/*
 * Copyright (c) 2016 Onegini B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "OGCDVAuthenticatorsClientHelper.h"
#import "OGCDVConstants.h"

@implementation OGCDVAuthenticatorsClientHelper

+ (NSDictionary *)dictionaryFromAuthenticator:(ONGAuthenticator *)authenticator
{
    NSString *type = [self typeStringFromAuthenticatorType:authenticator.type];

    NSString *identifier;
    if (authenticator.type == ONGAuthenticatorPIN || authenticator.type == ONGAuthenticatorTouchID) {
        NSArray *identifierItems = [authenticator.identifier componentsSeparatedByString:@"."];
        identifier = [identifierItems lastObject];
    } else {
        identifier = authenticator.identifier;
    }

    NSDictionary *dictionary = @{
        OGCDVPluginKeyAuthenticatorType: type,
        OGCDVPluginKeyAuthenticatorId: identifier,
        OGCDVPluginKeyAuthenticatorIsRegistered: @(authenticator.isRegistered),
        OGCDVPluginKeyAuthenticatorIsPreferred: @(authenticator.isPreferred),
        OGCDVPluginKeyAuthenticatorName: authenticator.name
    };
    return dictionary;
}

+ (ONGAuthenticator *)authenticatorFromArguments:(NSSet<ONGAuthenticator *> *)authenticators options:
    (NSDictionary *)options
{
    NSString *authenticatorType = options[OGCDVPluginKeyAuthenticatorType];
    NSString *authenticatorId = options[OGCDVPluginKeyAuthenticatorId];

    if (authenticatorId == nil) {
        return [self findAuthenticator:authenticators byType:authenticatorType];
    } else {
        return [self findAuthenticator:authenticators byType:authenticatorType andId:authenticatorId];
    }
}

+ (ONGAuthenticator *)findAuthenticator:(NSSet<ONGAuthenticator *> *)authenticators byType:(NSString *)authenticatorType
{
    for (ONGAuthenticator *authenticator in authenticators) {
        NSString *type = [self typeStringFromAuthenticatorType:authenticator.type];
        if ([ type isEqualToString:authenticatorType]) {
            return authenticator;
        }
    }

    return nil;
}

+ (ONGAuthenticator *)findAuthenticator:(NSSet<ONGAuthenticator *> *)authenticators byType:(NSString *)authenticatorType andId:(NSString *)authenticatorId
{
    for (ONGAuthenticator *authenticator in authenticators) {
        NSString *type = [self typeStringFromAuthenticatorType:authenticator.type];
        NSString *id = authenticator.identifier;

        if (id == nil) {
            continue;
        }

        if ([type isEqualToString:authenticatorType] && [id isEqualToString:authenticatorId]) {
            return authenticator;
        }
    }

    return nil;
}

+ (NSString *)typeStringFromAuthenticatorType:(ONGAuthenticatorType)authenticatorType
{
    switch (authenticatorType) {
        case ONGAuthenticatorPIN:
            return OGCDVPluginAuthenticatorTypePin;
        case ONGAuthenticatorTouchID:
            return OGCDVPluginAuthenticatorTypeTouchId;
        case ONGAuthenticatorFIDO:
            return OGCDVPluginAuthenticatorTypeFido;
        default:
            return nil;
    }
}

@end