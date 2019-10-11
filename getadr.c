//
// Modified getaddrinfo lookup tool from BeeJ's guide
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>

int main(int argc, char *argv[])
{
    struct addrinfo hints, *res, *p;
    int status;
    char ipstr[INET6_ADDRSTRLEN];
    if (argc != 2) {
        fprintf(stderr,"usage: showip hostname\n");
        return 1;
    }
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC; // AF_INET or AF_INET6 to force version
  //  hints.ai_socktype = SOCK_STREAM;
    if ((status = getaddrinfo(argv[1], NULL, &hints, &res)) != 0) {
        fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(status));
        return 2;
    }
    printf("IP data for %s:\n\n", argv[1]);
    for(p = res;p != NULL; p = p->ai_next) {
        void *addr;
        char *ipver, *sock_type, *protocol;
        int allocmem_sock = 0;
        int allocmem_prot = 0;
// get the pointer to the address itself,
// different fields in IPv4 and IPv6:
        if (p->ai_family == AF_INET) { // IPv4
            struct sockaddr_in *ipv4 = (struct sockaddr_in *)p->ai_addr;
            addr = &(ipv4->sin_addr);
            ipver = "IPv4";
        } else { // IPv6
            struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *)p->ai_addr;
            addr = &(ipv6->sin6_addr);
            ipver = "IPv6";
        }
        switch (p->ai_socktype) {
            case SOCK_STREAM:
                sock_type = "SOCK_STREAM";
                break;
            case SOCK_DGRAM:
                sock_type = "SOCK_DGRAM";
                break;
            case SOCK_RAW:
                sock_type = "SOCK_RAW";
                break;
            case SOCK_RDM:
                sock_type = "SOCK_RDM";
                break;
            case SOCK_SEQPACKET:
                sock_type = "SOCK_SEQPACKET";
                break;
            case SOCK_DCCP:
                sock_type = "SOCK_DCCP";
                break;
            case SOCK_PACKET:
                sock_type = "SOCK_PACKET";
                break;
            default:
                sock_type = (char*) malloc(sizeof(char) * 4);
                if (sock_type != 0) {
                    allocmem_sock = 1;
                    snprintf(sock_type, 4, "%d", p->ai_socktype);
                } else {
                    printf("Memory allocation error.\n");
                    return 1;
                }
                break;
        }
        switch (p->ai_protocol) {
            case IPPROTO_IP:
                protocol = "IPPROTO_IP";
                break;
            case IPPROTO_ICMP:
                protocol = "IPPROTO_ICMP";
                break;
            case IPPROTO_IGMP:
                protocol = "IPPROTO_IGMP";
                break;
            case IPPROTO_IPIP:
                protocol = "IPPROTO_IPIP";
                break;
            case IPPROTO_TCP:
                protocol = "IPPROTO_TCP";
                break;
            case IPPROTO_EGP:
                protocol = "IPPROTO_EGP";
                break;
            case IPPROTO_PUP:
                protocol = "IPPROTO_PUP";
                break;
            case IPPROTO_UDP:
                protocol = "IPPROTO_UDP";
                break;
            case IPPROTO_IDP:
                protocol = "IPPROTO_IDP";
                break;
            case IPPROTO_TP:
                protocol = "IPPROTO_TP";
                break;
            case IPPROTO_DCCP:
                protocol = "IPPROTO_DCCP";
                break;
            case IPPROTO_IPV6:
                protocol = "IPPROTO_IPV6";
                break;
            case IPPROTO_RSVP:
                protocol = "IPPROTO_RSVP";
                break;
            case IPPROTO_GRE:
                protocol = "IPPROTO_GRE";
                break;
            case IPPROTO_ESP:
                protocol = "IPPROTO_ESP";
                break;
            case IPPROTO_AH:
                protocol = "IPPROTO_AH";
                break;
            case IPPROTO_MTP:
                protocol = "IPPROTO_MTP";
                break;
            case IPPROTO_BEETPH:
                protocol = "IPPROTO_BEETPH";
                break;
            case IPPROTO_ENCAP:
                protocol = "IPPROTO_ENCAP";
                break;
            case IPPROTO_PIM:
                protocol = "IPPROTO_PIM";
                break;
            case IPPROTO_COMP:
                protocol = "IPPROTO_COMP";
                break;
            case IPPROTO_SCTP:
                protocol = "IPPROTO_SCTP";
                break;
            case IPPROTO_UDPLITE:
                protocol = "IPPROTO_UDPLITE";
                break;
            case IPPROTO_MPLS:
                protocol = "IPPROTO_MPLS";
                break;
            case IPPROTO_RAW:
                protocol = "IPPROTO_RAW";
                break;
            default:
                protocol = (char*) malloc(sizeof(char) * 5);
                if (protocol != 0) {
                    allocmem_prot = 1;
                    snprintf(protocol, 5, "%d", p->ai_protocol);
                } else {
                    printf("Memory allocation error.\n");
                    return 1;
                }
                break;
        }
// convert the IP to a string and print it:
        inet_ntop(p->ai_family, addr, ipstr, sizeof ipstr);
        printf(" %s: %s %s %s\n", ipver, ipstr, sock_type, protocol);
        if (allocmem_sock) {
            free(sock_type);
            allocmem_sock = 0;
        }
        if (allocmem_prot) {
            free(protocol);
            allocmem_prot = 0;
        }
    }
    freeaddrinfo(res); // free the linked list
    return 0;
}